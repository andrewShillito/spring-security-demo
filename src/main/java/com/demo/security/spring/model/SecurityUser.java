package com.demo.security.spring.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "security_users")
@Getter
@Setter
@ToString(exclude = {"username", "password"}) // don't want these in logs
@SequenceGenerator(name = "security_users_id_seq", sequenceName = "security_users_id_seq", allocationSize = 50)
public class SecurityUser implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "security_users_id_seq")
  private Long id;

  @NotBlank
  private String username;

  @NotBlank
  private String email;

  @NotBlank
  private String password;

  @Column(name = "user_type")
  @Enumerated(EnumType.STRING)
  @NotNull
  private UserType userType = UserType.external;

  @NotBlank
  private String userRole = "STANDARD";

  private boolean enabled;

  @Column(name = "account_expired")
  private boolean accountExpired;

  @Column(name = "account_expired_date")
  private ZonedDateTime accountExpiredDate;

  @Column(name = "password_expired")
  private boolean passwordExpired;

  @Column(name = "password_expired_date")
  private ZonedDateTime passwordExpiredDate;

  private boolean locked;

  @Column(name = "locked_date")
  private ZonedDateTime lockedDate;

  @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  private List<SecurityAuthority> authorities;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  public void setAuthorities(List<SecurityAuthority> authorities) {
    authorities.forEach(auth -> { if (auth.getUser() == null) { auth.setUser(this); } });
    this.authorities = authorities;
  }

  @Override
  public boolean isAccountNonExpired() {
    return !accountExpired;
  }

  @Override
  public boolean isAccountNonLocked() {
    return !locked;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return !passwordExpired;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }
}
