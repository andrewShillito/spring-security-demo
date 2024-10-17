package com.demo.security.spring.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "security_groups")
@Getter
@Setter
@SequenceGenerator(name = "security_groups_id_seq", sequenceName = "security_groups_id_seq", allocationSize = 50, initialValue = 1)
@JsonInclude(Include.NON_EMPTY)
@ToString(exclude = { "users", "authorities" } )
@EqualsAndHashCode
public class SecurityGroup {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "security_groups_id_seq")
  private Long id;

  @NotBlank
  @Column(name = "code", length = 100, nullable = false)
  private String code;

  @NotBlank
  @Column(name = "description", length = 255, nullable = false)
  private String description;

  @Transient
  private Set<SecurityUser> users = new HashSet<>();

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "security_groups_roles",
      joinColumns = @JoinColumn(name = "group_id"),
      inverseJoinColumns = @JoinColumn(name = "authority_id")
  )
  private Set<SecurityAuthority> authorities;

}
