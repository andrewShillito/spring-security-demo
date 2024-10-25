package com.demo.security.spring.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "security_groups_roles")
@Getter
@Setter
@ToString(exclude = { "group", "authority" })
@EqualsAndHashCode
public class SecurityGroupAuthority {

  @EmbeddedId
  private SecurityGroupAuthorityKey id;

  @ManyToOne(fetch = FetchType.EAGER)
  @MapsId("groupId")
  @JoinColumn(name = "group_id")
  @NotNull
  private SecurityGroup group;

  @ManyToOne(fetch = FetchType.EAGER)
  @MapsId("authorityId")
  @JoinColumn(name = "authority_id")
  @NotNull
  private SecurityAuthority authority;

}
