package com.demo.security.spring.model;

import java.util.HashSet;
import java.util.Set;
import lombok.Getter;

/**
 * Used to move user role and authority information to/from example-roles-config.json
 */
@Getter
public class SecurityGroupConfig {

  private String groupName;

  private Set<String> authorities = new HashSet<>();

  public SecurityGroupConfig setGroupName(String groupName) {
    this.groupName = groupName;
    return this;
  }

  public SecurityGroupConfig setAuthorities(Set<String> authorities) {
    this.authorities = authorities;
    return this;
  }

  @Override
  public String toString() {
    return "SecurityGroupConfig{" +
        "groupName='" + groupName + '\'' +
        ", authorities=" + authorities +
        '}';
  }
}
