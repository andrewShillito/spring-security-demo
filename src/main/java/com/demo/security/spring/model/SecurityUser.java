package com.demo.security.spring.model;

import jakarta.persistence.*;
import java.time.ZonedDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "security_user")
@Getter
@Setter
@ToString(exclude = {"username", "password"}) // don't want these in logs
public class SecurityUser implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String email;

    private String password;

    @Column(name = "user_type")
    @Enumerated(EnumType.STRING)
    private UserType userType;

    private String userRole;

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

    @OneToMany(mappedBy = "user")
    private List<SecurityAuthorities> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
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
