package com.demo.security.spring.authentication;

import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.repository.SecurityUserRepository;
import com.demo.security.spring.utils.SpringProfileConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
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
  public void setUserRepository(SecurityUserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Autowired
  public void setPasswordEncoder(
      PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    final String username = authentication.getName();
    final String password = authentication.getCredentials().toString();
    if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
      throw new BadCredentialsException("Missing required credentials for username or password");
    }
    final SecurityUser user = userRepository.getSecurityUserByUsername(username);
    validateUser(username, password, user);
    return new UsernamePasswordAuthenticationToken(username, password, user.getAuthorities());
  }

  private void validateUser(final String username, final String providedPassword, final SecurityUser user) {
    if (user == null) {
      throw new BadCredentialsException("No user registered with username " + username);
    } else if (!passwordEncoder.matches(providedPassword, user.getPassword())) {
      throw new BadCredentialsException("Invalid credentials");
    } else if (!user.isEnabled()) {
      throw new BadCredentialsException("The account for user " + username + " is not yet enabled");
    } else if (user.isLocked()) {
      throw new BadCredentialsException("The account for user " + username + " has been locked");
    } else if (user.isAccountExpired()) {
      throw new BadCredentialsException("The account for user " + username + " has expired");
    } else if (user.getAuthorities() == null || user.getAuthorities().isEmpty()) {
      // note that authorities are fetched eagerly by hibernate
      throw new IllegalStateException("User " + username + " has no related authorities!");
    }
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
