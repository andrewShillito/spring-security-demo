package com.demo.security.spring.service;

import com.demo.security.spring.model.PasswordWrapper;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.repository.SecurityUserRepository;
import com.demo.security.spring.utils.SecurityUtils;
import com.google.common.base.Preconditions;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;
import java.util.Set;
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

  private Validator validator;

  @Override
  public void createUser(final UserDetails user) {
    if (user == null) {
      throw new AssertionError("User to create is null");
    } else if (!(user instanceof SecurityUser)) {
      throw new IllegalArgumentException("User details type is unsupported. Expected " + SecurityUser.class.getName() + " but was " + user.getClass().getName());
    }
    final SecurityUser securityUser = (SecurityUser) user;
    // validate password prior to saving as once encoded we cannot validate it as easily
    final Set<ConstraintViolation<PasswordWrapper>> passwordErrors = validateUserPassword(securityUser);
    if (passwordErrors != null && !passwordErrors.isEmpty()) {
      throw new AssertionError("User password failed validation");
    }
    securityUser.setPassword(passwordEncoder.encode(securityUser.getPassword()));
    userRepository.save(securityUser);
  }

  @Override
  public void updateUser(UserDetails user) {
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

  /**
   * Return the {@link SecurityUser} for the given authentication or null.
   * Users {@link #loadUserByUsername(String)} so can throw UsernameNotFoundException
   * @param authentication the authentication to get the associated user for
   * @return the SecurityUser for the given authentication or null
   * @throws UsernameNotFoundException if user matching that username is not found
   */
  public SecurityUser getAuthenticatedUser(Authentication authentication) {
    if (authentication != null && StringUtils.isNotBlank(authentication.getName())) {
      return (SecurityUser) loadUserByUsername(authentication.getName());
    }
    return null;
  }

  /**
   * Get the currently authenticated {@link SecurityUser}.
   * @return the user associated with authentication in SecurityContextHolder or null
   */
  public SecurityUser getAuthenticatedUser() {
    final String principalName = SecurityUtils.getPrincipalName();
    if (StringUtils.isNotBlank(principalName)) {
      try {
        return (SecurityUser) loadUserByUsername(principalName);
      } catch (UsernameNotFoundException e) {
        log.error(() -> "User with username " + principalName + " not found despite being authenticated!", e);
      } catch (ClassCastException ex) {
        log.error(() -> "Unknown userDetails class implementation found!", ex);
      }
    }
    return null;
  }

  public Set<ConstraintViolation<PasswordWrapper>> validateUserPassword(SecurityUser user) {
    return validator.validateProperty(new PasswordWrapper().setPassword(user.getPassword()),
        "password", Default.class);
  }
}
