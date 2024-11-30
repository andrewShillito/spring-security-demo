package com.demo.security.spring.service;

import com.demo.security.spring.model.SecurityUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface SecurityUserService {

  void createUser(final SecurityUser user);

  void updateUser(SecurityUser user);

  void deleteUser(String username);

  void changePassword(String oldPassword, String newPassword);

  boolean userExists(String username);

  SecurityUser loadUserByUsername(String username) throws UsernameNotFoundException;

  SecurityUser getAuthenticatedUser(Authentication authentication);

  SecurityUser getAuthenticatedUser();

  void incrementFailedLogons(SecurityUser user);

}
