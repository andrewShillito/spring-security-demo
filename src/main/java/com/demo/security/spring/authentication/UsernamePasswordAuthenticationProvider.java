package com.demo.security.spring.authentication;

import com.demo.security.spring.model.AuthenticationFailureReason;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.repository.SecurityUserRepository;
import com.demo.security.spring.utils.SpringProfileConstants;
import java.time.ZonedDateTime;
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
@Profile(SpringProfileConstants.POSTGRES)
public class UsernamePasswordAuthenticationProvider implements AuthenticationProvider {

  private SecurityUserRepository userRepository;

  private PasswordEncoder passwordEncoder;

  @Autowired
  private AuthenticationAttemptManager authenticationAttemptManager;

  @Autowired
  public void setUserRepository(SecurityUserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Autowired
  public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    final String username = authentication.getName();
    final String password = authentication.getCredentials().toString();
    if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
      authenticationAttemptManager.handleFailedAuthentication(username, null, AuthenticationFailureReason.BAD_CREDENTIALS);
      throw new BadCredentialsException("Missing required credentials for username or password");
    }
    final SecurityUser user = userRepository.getSecurityUserByUsername(username);
    validateUser(username, password, user);
    authenticationAttemptManager.handleSuccessfulAuthentication(user);

    return new UsernamePasswordAuthenticationToken(username, password, user.getAuthorities());
  }

  private void validateUser(final String username, final String providedPassword, SecurityUser user) {
    if (user == null) {
      authenticationAttemptManager.handleFailedAuthentication(username, user, AuthenticationFailureReason.USER_NOT_FOUND);
      throw new UsernameNotFoundException("No account matching username " + username);
    }
    if (!user.isEnabled()) {
      authenticationAttemptManager.handleFailedAuthentication(username, user, AuthenticationFailureReason.DISABLED);
      throw new DisabledException("The account for user " + username + " is not enabled");
    }
    if (user.isLocked()) {
      if (user.getUnlockDate() != null && ZonedDateTime.now().isAfter(user.getUnlockDate())) {
        // clear user lockout and continue on
        user.clearLockout();
        user = userRepository.save(user);
      } else {
        authenticationAttemptManager.handleFailedAuthentication(username, user, AuthenticationFailureReason.LOCKED);
        throw new LockedException("The account for user " + username + " is locked");
      }
    }
    if (!user.isCredentialsNonExpired()) {
      authenticationAttemptManager.handleFailedAuthentication(username, user, AuthenticationFailureReason.CREDENTIALS_EXPIRED);
      throw new CredentialsExpiredException("The credentials for user " + username + " are expired");
    }
    if (user.isAccountExpired()) {
      authenticationAttemptManager.handleFailedAuthentication(username, user, AuthenticationFailureReason.ACCOUNT_EXPIRED);
      throw new AccountExpiredException("The account for user " + username + " has expired");
    }
    if (user.getAuthorities() == null || user.getAuthorities().isEmpty()) {
      authenticationAttemptManager.handleFailedAuthentication(username, user, AuthenticationFailureReason.NO_AUTHORITIES);
      // note that authorities are fetched eagerly by hibernate
      throw new IllegalStateException("User " + username + " has no related authorities!");
    }
    if (!passwordEncoder.matches(providedPassword, user.getPassword())) {
      user.incrementFailedLoginAttempts(); // can result in lockout
      userRepository.save(user);
      authenticationAttemptManager.handleFailedAuthentication(username, user, AuthenticationFailureReason.BAD_CREDENTIALS);
      throw new BadCredentialsException("Invalid credentials");
    }
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }
}