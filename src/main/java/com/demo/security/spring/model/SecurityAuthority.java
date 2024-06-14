package com.demo.security.spring.model;

import com.demo.security.spring.validation.IsValidRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;

@Getter
@Setter
@ToString(exclude = "user")
@Entity
@Table(name = "security_authorities")
@SequenceGenerator(name = "security_authorities_id_seq", sequenceName = "security_authorities_id_seq", allocationSize = 50)
public class SecurityAuthority implements GrantedAuthority {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "security_authorities_id_seq")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, updatable = false, insertable = true)
  @JsonIgnore
  @NotNull
  private SecurityUser user;

  @NotBlank
  @IsValidRole
  private String authority;

}
