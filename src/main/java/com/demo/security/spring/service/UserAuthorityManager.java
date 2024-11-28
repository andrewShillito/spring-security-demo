package com.demo.security.spring.service;

import com.demo.security.spring.model.SecurityAuthority;
import com.demo.security.spring.model.SecurityGroup;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.repository.SecurityAuthorityRepository;
import com.demo.security.spring.repository.SecurityGroupRepository;
import com.demo.security.spring.repository.SecurityUserRepository;
import java.util.Collection;
import java.util.Set;
import javax.annotation.Nonnull;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Builder
public class UserAuthorityManager {

  private SecurityUserRepository userRepository;

  private SecurityAuthorityRepository authorityRepository;

  private SecurityGroupRepository groupRepository;

  @Transactional
  public SecurityUser addAuthorities(@Nonnull SecurityUser user, @Nonnull Collection<String> authorities) {
    if (!authorities.isEmpty()) {
      Set<SecurityAuthority> auths = authorityRepository.findAllByAuthorityIn(authorities);
      if (auths.isEmpty() || auths.size() != authorities.size()) {
        log.error(() -> "Received invalid authorities to add for user '" + user.getUsername() + "'. Requested: " + authorities + ", actual: " + auths);
      }
      if (!auths.isEmpty()) {
        auths.forEach(user::addAuthority);
        userRepository.save(user);
      }
    }
    return user;
  }

  @Transactional
  public SecurityUser addSecurityAuthorities(@Nonnull SecurityUser user, @Nonnull Collection<SecurityAuthority> authorities) {
    if (!authorities.isEmpty()) {
      int startingSize = user.getSecurityAuthorities().size();
      authorities.forEach(auth -> {
        if (auth == null || StringUtils.isBlank(auth.getAuthority())) {
          log.error("Received invalid authority '" + auth + "'to add to user '" + user.getUsername() + "'");
        } else {
          user.addAuthority(auth);
        }
      });
      if (user.getSecurityAuthorities().size() != startingSize) {
        userRepository.save(user);
      } else {
        log.warn(() -> "User '" + user.getUsername() + "' already has all authorities attempt was made to add " + authorities);
      }
    }
    return user;
  }

  @Transactional
  public SecurityUser addAuthority(@Nonnull SecurityUser user, @Nonnull String authority) {
    if (StringUtils.isNotBlank(authority)) {
      log.info(() -> "Adding authority " + authority + " to user with name " + user.getUsername());
      SecurityAuthority securityAuthority = authorityRepository.findByAuthorityEquals(authority);
      if (securityAuthority != null) {
        return addAuthority(user, securityAuthority);
      } else {
        log.error(() -> "Cannot add unknown authority with name '" + authority + "' to user '" + user.getUsername() + "'");
      }
    } else {
      log.error(() -> "Cannot add blank authority '" + authority + "' to user " + user.getUsername());
    }
    return user;
  }

  @Transactional
  public SecurityUser addAuthority(@Nonnull SecurityUser user, @Nonnull SecurityAuthority authority) {
    user.addAuthority(authority);
    return userRepository.save(user);
  }

  @Transactional
  public SecurityUser addGroup(SecurityUser user, String group) {
    if (StringUtils.isNotBlank(group)) {
      log.info(() -> "Adding group " + group + " to user with name " + user.getUsername());
      SecurityGroup securityGroup = groupRepository.findByCodeEquals(group);
      if (securityGroup != null) {
        return addGroup(user, securityGroup);
      } else {
        log.error(() -> "Cannot add unknown group with name '" + group + "' to user '" + user.getUsername() + "'");
      }
    } else {
      log.error(() -> "Cannot add blank group '" + group + "' to user " + user.getUsername());
    }
    return user;
  }

  @Transactional
  public SecurityUser addGroup(@Nonnull SecurityUser user, @Nonnull SecurityGroup group) {
    user.addGroup(group);
    return userRepository.save(user);
  }

}
