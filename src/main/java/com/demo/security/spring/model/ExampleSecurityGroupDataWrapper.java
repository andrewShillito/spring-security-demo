package com.demo.security.spring.model;

import java.util.List;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ExampleSecurityGroupDataWrapper {

  private List<SecurityGroupConfig> groupConfigs;

  private List<SecurityGroup> groups;

  public ExampleSecurityGroupDataWrapper setGroupConfigs(
      List<SecurityGroupConfig> groupConfigs) {
    this.groupConfigs = groupConfigs;
    return this;
  }

  public ExampleSecurityGroupDataWrapper setGroups(List<SecurityGroup> groups) {
    this.groups = groups;
    return this;
  }

  public boolean hasGroups() {
    return groups != null && !groups.isEmpty();
  }

  public boolean hasGroupsConfig() {
    return groupConfigs != null && !groupConfigs.isEmpty();
  }
}
