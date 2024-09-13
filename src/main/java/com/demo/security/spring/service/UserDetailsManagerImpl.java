package com.demo.security.spring.service;

import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.repository.SecurityUserRepository;
import com.demo.security.spring.utils.SecurityUtils;
import com.google.common.base.Preconditions;
import javax.naming.AuthenticationException;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;

@Builder
@Log4j2
public class UserDetailsManagerImpl implements UserDetailsManager {

  private SecurityUserRepository userRepository;

  private AuthenticationManager authenticationManager;

  private SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();

  private PasswordEncoder passwordEncoder;

  private SecurityUserValidationService userValidationService;

  @Override
  public void createUser(final UserDetails user) {
    userValidationService.validateUser(user, true);
    final SecurityUser securityUser = (SecurityUser) user;
    securityUser.setPassword(passwordEncoder.encode(securityUser.getPassword()));
    userRepository.save(securityUser);
  }

  @Override
  public void updateUser(UserDetails user) {
    userValidationService.validateUser(user, false);
    SecurityUser securityUser = (SecurityUser) user;
    // TODO: implement this method
  }

  @Override
  public void deleteUser(String username) {
    // TODO: implement this method
  }

  @Override
  public void changePassword(String oldPassword, String newPassword) {
    if (StringUtils.isBlank(newPassword)) {
      throw new IllegalArgumentException("Cannot change password to empty new password");
    }
    final String username = SecurityUtils.getPrincipalName();
    if (StringUtils.isBlank(username)) {
      throw new RuntimeException(new AuthenticationException("Cannot change password as user is not logged in!"));
    }
    Authentication authentication = authenticationManager.authenticate(
        UsernamePasswordAuthenticationToken.unauthenticated(username, oldPassword));
    if (!authentication.isAuthenticated()) {
      throw new RuntimeException(new AuthenticationException("User password does not match"));
    }
    log.debug(() -> "Changing user " + username + " password");
    if (userRepository.updateUserPassword(username, passwordEncoder.encode(newPassword)) != 1) {
      throw new RuntimeException("Failed to update user " + username + " password");
    }
    securityContextHolderStrategy.clearContext();
    SecurityContext newSecurityContext = securityContextHolderStrategy.createEmptyContext();
    Authentication newAuthentication = createNewAuthentication(authentication, newPassword);
    newSecurityContext.setAuthentication(newAuthentication);
    securityContextHolderStrategy.setContext(newSecurityContext);
  }

  protected Authentication createNewAuthentication(Authentication currentAuth, String newPassword) {
    UserDetails user = loadUserByUsername(currentAuth.getName());
    UsernamePasswordAuthenticationToken newAuthentication = UsernamePasswordAuthenticationToken.authenticated(user,
        newPassword, user.getAuthorities());
    newAuthentication.setDetails(user); // TODO: check this - jdbc impl from spring sets this to old auth user details, not new
    return newAuthentication;
  }

  @Override
  public boolean userExists(String username) {
    if (StringUtils.isNotBlank(username)) {
      return userRepository.existsByUsernameIgnoreCase(username);
    }
    return false;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Preconditions.checkArgument(StringUtils.isNotBlank(username),
        "Username " + username + " is not valid");
    final SecurityUser user = userRepository.getSecurityUserByUsername(username);
    if (user == null) {
      throw new UsernameNotFoundException("User with username " + username + " was not found.");
    }
    return user;
  }
}
