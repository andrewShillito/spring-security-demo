package com.demo.security.spring.service;

import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.utils.SecurityUtils;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;

@Builder
@Log4j2
public class SecurityUserServiceImpl implements SecurityUserService {

  private UserDetailsManager userDetailsManager;

  @Override
  public void createUser(final SecurityUser user) {
    userDetailsManager.createUser(user);
  }

  @Override
  public void updateUser(SecurityUser user) {
    userDetailsManager.updateUser(user);
  }

  @Override
  public void deleteUser(String username) {
    userDetailsManager.deleteUser(username);
  }

  @Override
  public void changePassword(String oldPassword, String newPassword) {
    userDetailsManager.changePassword(oldPassword, newPassword);
  }

  @Override
  public boolean userExists(String username) {
    return userDetailsManager.userExists(username);
  }

  @Override
  public SecurityUser loadUserByUsername(String username) throws UsernameNotFoundException {
    return (SecurityUser) userDetailsManager.loadUserByUsername(username);
  }

  /**
   * Return the {@link SecurityUser} for the given authentication or null.
   * Users {@link #loadUserByUsername(String)} so can throw UsernameNotFoundException
   * @param authentication the authentication to get the associated user for
   * @return the SecurityUser for the given authentication or null
   * @throws UsernameNotFoundException if user matching that username is not found
   */
  @Override
  public SecurityUser getAuthenticatedUser(Authentication authentication) {
    if (authentication != null && StringUtils.isNotBlank(authentication.getName())) {
      return loadUserByUsername(authentication.getName());
    }
    return null;
  }

  /**
   * Get the currently authenticated {@link SecurityUser}.
   * @return the user associated with authentication in SecurityContextHolder or null
   */
  @Override
  public SecurityUser getAuthenticatedUser() {
    final String principalName = SecurityUtils.getPrincipalName();
    if (StringUtils.isNotBlank(principalName)) {
      try {
        return loadUserByUsername(principalName);
      } catch (UsernameNotFoundException e) {
        log.error(() -> "User with username " + principalName + " not found despite being authenticated!", e);
      } catch (ClassCastException ex) {
        log.error(() -> "Unknown userDetails class implementation found!", ex);
      }
    }
    return null;
  }

  @Override
  public void incrementFailedLogons(SecurityUser user) {
    if (user == null) {
      log.error(() -> "Cannot increment failed logon for null user");
      return;
    }
    user.incrementFailedLoginAttempts();
    updateUser(user);
  }
}
