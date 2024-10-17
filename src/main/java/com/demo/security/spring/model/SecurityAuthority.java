package com.demo.security.spring.model;

import com.demo.security.spring.validation.IsValidRole;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
import org.springframework.security.core.GrantedAuthority;

@Getter
@Setter
@ToString(exclude = { "groups", "users" })
@Entity
@Table(name = "security_authorities", indexes = {
    @Index(name = "ix_auth_user_id", columnList = "user_id,authority", unique = true)
})
@SequenceGenerator(name = "security_authorities_id_seq", sequenceName = "security_authorities_id_seq", allocationSize = 50, initialValue = 1)
@JsonInclude(Include.NON_EMPTY)
@EqualsAndHashCode
public class SecurityAuthority implements GrantedAuthority {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "security_authorities_id_seq")
  private Long id;

  @Transient
  private Set<SecurityUser> users = new HashSet<>();

  @NotBlank
  @IsValidRole
  @Column(name = "authority", length = 100, nullable = false)
  private String authority;

  @NotBlank
  @Column(name = "description", length = 255, nullable = false)
  private String description;

  @Transient
  private Set<SecurityGroup> groups = new HashSet<>();
}
