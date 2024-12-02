package com.demo.security.spring.service;

import com.demo.security.spring.controller.error.DuplicateUserException;
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

  private PasswordEncoder passwordEncoder;

  private SecurityUserValidationService userValidationService;

  private UserCache userCache;

  @Override
  public void createUser(final UserDetails user) {
    userValidationService.validateUser(user, true);
    if (userRepository.existsByUsernameIgnoreCase(user.getUsername())) {
      throw new DuplicateUserException(user.getUsername());
    }
    final SecurityUser securityUser = (SecurityUser) user;
    securityUser.setPassword(passwordEncoder.encode(securityUser.getPassword()));
    userCache.put(userRepository.save(securityUser));
  }

  @Override
  public void updateUser(UserDetails user) {
    userValidationService.validateUser(user, false);
    SecurityUser securityUser = (SecurityUser) user;
    userCache.put(userRepository.save(securityUser));
  }

  @Override
  public void deleteUser(String username) {
    if (StringUtils.isNotBlank(username)) {
      SecurityUser toDelete = userRepository.getSecurityUserByUsername(username);
      if (toDelete != null) {
        userRepository.delete(toDelete);
        userCache.invalidate(username);
      }
    }
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
    userCache.invalidate(username);
    SecurityContextHolderStrategy contextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
    contextHolderStrategy.clearContext();
    SecurityContext newSecurityContext = contextHolderStrategy.createEmptyContext();
    SecurityUser user = (SecurityUser) loadUserByUsername(authentication.getName());
    UsernamePasswordAuthenticationToken newAuthentication = UsernamePasswordAuthenticationToken.authenticated(user, newPassword, user.getAuthorities());
    newAuthentication.setDetails(user);
    newSecurityContext.setAuthentication(newAuthentication);
    contextHolderStrategy.setContext(newSecurityContext);
    userCache.put(user);
  }

  @Override
  public boolean userExists(String username) {
    if (StringUtils.isNotBlank(username)) {
      return userCache.isPresent(username) || userRepository.existsByUsernameIgnoreCase(username);
    }
    return false;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Preconditions.checkArgument(StringUtils.isNotBlank(username),
        "Username " + username + " is not valid");
    SecurityUser user = userCache.get(username);
    if (user == null) {
      user = userRepository.getSecurityUserByUsername(username);
    }
    if (user == null) {
      throw new UsernameNotFoundException("User with username " + username + " was not found.");
    }
    return user;
  }
}
