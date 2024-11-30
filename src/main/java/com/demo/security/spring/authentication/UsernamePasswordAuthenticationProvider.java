package com.demo.security.spring.authentication;

import com.demo.security.spring.model.SecurityAuthority;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.repository.SecurityUserRepository;
import com.demo.security.spring.service.UserCache;
import com.demo.security.spring.utils.SpringProfileConstants;
import java.time.ZonedDateTime;
import java.util.Set;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * A small example username + password authentication provider.
 * This is only implemented for the postgres spring profile currently.
 * InMemoryUserDetailsManager instead uses default spring-security authentication providers.
 */
@Component
@Profile({ SpringProfileConstants.POSTGRES, SpringProfileConstants.H2 })
@Log4j2
public class UsernamePasswordAuthenticationProvider implements AuthenticationProvider {

  private SecurityUserRepository userRepository;

  private PasswordEncoder passwordEncoder;

  private UserCache userCache;

  @Autowired
  public void setUserRepository(SecurityUserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Autowired
  public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
  }

  @Autowired
  public void setUserCache(UserCache userCache) {
    this.userCache = userCache;
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    final String username = authentication.getName();
    final String password = authentication.getCredentials().toString();
    if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
      throw new BadCredentialsException("Missing required credentials for username or password");
    }
    SecurityUser user = userCache.get(username);
    if (user == null) {
      user = userRepository.getSecurityUserByUsername(username);
    }
    validateUser(username, password, user);
    return new UsernamePasswordAuthenticationToken(username, password, user.getAuthorities());
  }

  private void validateUser(final String username, final String providedPassword, SecurityUser user) {
    if (user == null) {
      throw new UsernameNotFoundException("No account matching username " + username);
    }
    if (!user.isEnabled()) {
      throw new DisabledException("The account for user " + username + " is not enabled");
    }
    if (!user.isCredentialsNonExpired()) {
      throw new CredentialsExpiredException("The credentials for user " + username + " are expired");
    }
    if (user.isAccountExpired()) {
      throw new AccountExpiredException("The account for user " + username + " has expired");
    }
    Set<SecurityAuthority> authorities = user.getAuthorities();
    if (authorities.isEmpty()) {
      log.warn(() -> "User " + username + " has no related authorities!");
    }
    if (!passwordEncoder.matches(providedPassword, user.getPassword())) {
      throw new BadCredentialsException("Invalid credentials");
    }
    if (user.isLocked()) { // we know that the credentials matched by the time we get here
      if (user.getUnlockDate() != null && ZonedDateTime.now().isAfter(user.getUnlockDate())) {
        // clear user lockout and continue on
        user.clearLockout();
        user = userRepository.save(user);
        userCache.put(user);
      } else {
        throw new LockedException("The account for user " + username + " is locked");
      }
    }
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }
}