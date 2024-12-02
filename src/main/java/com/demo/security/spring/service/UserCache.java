package com.demo.security.spring.service;

import com.demo.security.spring.model.SecurityUser;
import com.google.common.cache.Cache;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

@Log4j2
@Builder
public class UserCache {

  private Cache<String, SecurityUser> cache;

  private String normalizeKey(String s) {
    if (s != null) {
      return s.toUpperCase();
    }
    return null;
  }

  public void put(SecurityUser user) {
    if (user != null && StringUtils.isNotBlank(user.getUsername())) {
      cache.put(normalizeKey(user.getUsername()), user);
    }
  }

  public boolean isPresent(String username) {
    return StringUtils.isNotBlank(username) && get(username) != null;
  }

  public void invalidate(String username) {
    if (username != null) {
      cache.invalidate(normalizeKey(username));
    }
  }

  public SecurityUser get(String username) {
    if (StringUtils.isNotBlank(username)) {
      return cache.getIfPresent(normalizeKey(username));
    }
    return null;
  }

}
