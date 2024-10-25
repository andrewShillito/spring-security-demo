package com.demo.security.spring.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "security_users_groups")
@Getter
@Setter
@ToString(exclude = { "user", "group" })
@EqualsAndHashCode
public class SecurityUserGroup {

  @EmbeddedId
  private SecurityUserGroupKey id;

  @ManyToOne
  @MapsId("userId")
  @JoinColumn(name = "user_id")
  @NotNull
  private SecurityUser user;

  @ManyToOne
  @MapsId("groupId")
  @JoinColumn(name = "group_id")
  @NotNull
  private SecurityGroup group;

  public Set<SecurityAuthority> getAuthorities() {
    if (group != null) {
      return group.getAuthorities();
    }
    return new HashSet<>();
  }

}
