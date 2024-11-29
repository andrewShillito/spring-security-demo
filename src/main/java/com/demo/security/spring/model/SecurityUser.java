package com.demo.security.spring.model;

import com.demo.security.spring.utils.SecurityUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.google.common.base.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessageFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.ReflectionUtils;

@Entity
@Table(name = "security_users")
@Getter
@Setter
@ToString(exclude = { "password" }) // don't want password in logs
@SequenceGenerator(name = "security_users_id_seq", sequenceName = "security_users_id_seq", allocationSize = 50, initialValue = 1)
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@Log4j2
@EqualsAndHashCode
public class SecurityUser implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "security_users_id_seq")
  private Long id;

  @NotBlank
  @Column(name = "username", length = 100, unique = true, nullable = false)
  private String username;

  @NotBlank
  @Column(name = "email", length = 100, nullable = false)
  private String email;

  @Column(name = "password", length = 500, nullable = false)
  @JsonProperty(access = Access.WRITE_ONLY)
  @NotBlank
  private String password;

  @NotNull
  private boolean enabled;

  @Column(name = "account_expired", nullable = false)
  private boolean accountExpired;

  @Column(name = "account_expired_date")
  private ZonedDateTime accountExpiredDate;

  @Column(name = "password_expired", nullable = false)
  private boolean passwordExpired;

  @Column(name = "password_expired_date")
  private ZonedDateTime passwordExpiredDate;

  @Column(name = "failed_login_attempts")
  private int failedLoginAttempts = 0;

  @Column(name = "num_previous_lockouts")
  private int numPreviousLockouts = 0;

  @Column(name = "locked", nullable = false)
  private boolean locked;

  @Column(name = "locked_date")
  private ZonedDateTime lockedDate;

  @Column(name = "unlock_date")
  private ZonedDateTime unlockDate;

  // this supports single authorities being assigned to a user as well as roles which contain 0 to many roles
  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "security_users_authorities",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "authority_id")
  )
  @Setter
  private Set<SecurityAuthority> securityAuthorities;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "security_users_groups",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "group_id")
  )
  private Set<SecurityGroup> groups;

  @Column(name = "last_login_date")
  private ZonedDateTime lastLoginDate;

  @Embedded
  private EntityControlDates controlDates = new EntityControlDates();

  public SecurityUser() {
  }

  public SecurityUser(SecurityUser toClone) {
    this();
    Preconditions.checkNotNull(toClone);
    // does not clone id
    setUsername(toClone.getUsername());
    setPassword(toClone.getPassword());

    setEmail(toClone.getEmail());
    setEnabled(toClone.isEnabled());
    setAccountExpired(toClone.isAccountExpired());
    setAccountExpiredDate(toClone.getAccountExpiredDate());
    setPasswordExpired(toClone.isPasswordExpired());
    setPasswordExpiredDate(toClone.getPasswordExpiredDate());
    setFailedLoginAttempts(toClone.getFailedLoginAttempts());
    setNumPreviousLockouts(toClone.getNumPreviousLockouts());
    setLocked(toClone.isLocked());
    setLockedDate(toClone.getLockedDate());
    setUnlockDate(toClone.getUnlockDate());
    setGroups(new HashSet<>(toClone.getGroups()));
    setSecurityAuthorities(new HashSet<>(toClone.getSecurityAuthorities()));

    if (toClone.getControlDates() != null) {
      // ZonedDateTime is immutable but this still makes me a little uncomfortable
      EntityControlDates clonedControlDates = new EntityControlDates();
      clonedControlDates.setCreated(toClone.getControlDates().getCreated());
      clonedControlDates.setLastUpdated(toClone.getControlDates().getLastUpdated());
      setControlDates(clonedControlDates);
    }
  }

  /**
   * Returns the union of {@link #securityAuthorities} and {@link #groups#securityAuthorities}
   * or empty set if none present. Never returns null.
   * @return empty or populated set produced as the union of single authorities and role authorities applicable to this user
   */
  public Set<SecurityAuthority> getSecurityAuthorities() {
    return this.securityAuthorities != null ? securityAuthorities : new HashSet<>();
  }

  @Nonnull
  public Set<SecurityGroup> getGroups() {
    return this.groups == null ? new HashSet<>() : this.groups;
  }

  @Override
  @Nonnull
  @JsonIgnore
  public Set<SecurityAuthority> getAuthorities() {
    Set<SecurityAuthority> derivedAuthorities = new HashSet<>();

    if (this.securityAuthorities != null) {
      derivedAuthorities.addAll(this.securityAuthorities);
    }

    if (groups != null && !groups.isEmpty()) {
      groups.forEach(group -> {
        if (group != null && group.getAuthorities() != null && !group.getAuthorities().isEmpty()) {
          derivedAuthorities.addAll(group.getAuthorities());
        }
      });
    }
    return derivedAuthorities;
  }

  public void addAuthority(SecurityAuthority authority) {
    if (authority != null) {
      if (securityAuthorities == null) {
        securityAuthorities = new HashSet<>();
      }
      securityAuthorities.add(authority);
    }
  }

  public void addGroup(SecurityGroup group) {
    if (group != null) {
      if (groups == null) {
        groups = new HashSet<>();
      }
      groups.add(group);
    }
  }

  @Override
  @JsonIgnore
  public boolean isAccountNonExpired() {
    return !accountExpired;
  }

  @Override
  @JsonIgnore
  public boolean isAccountNonLocked() {
    return !locked;
  }

  @Override
  @JsonIgnore
  public boolean isCredentialsNonExpired() {
    return !passwordExpired;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  public void clearLockout() {
    setLocked(false);
    setLockedDate(null);
    setFailedLoginAttempts(0);
    setNumPreviousLockouts(1);
  }

  public void lock() {
    // TODO: adding adaptive lock out time ( increasing by number of recent attempts / lockouts ) would be nice
    setLocked(true);
    setLockedDate(ZonedDateTime.now());
    setUnlockDate(ZonedDateTime.now().plusMinutes(5));
  }

  public void incrementFailedLoginAttempts() {
    setFailedLoginAttempts(getFailedLoginAttempts() + 1);
    if (getFailedLoginAttempts() > SecurityUtils.MAX_ALLOWED_FAILED_LOGIN_ATTEMPTS) {
      lock();
    }
  }

  /**
   * Returns a representation of this object as a map which is used for exporting local
   * example test/development environment data to example-users.json.
   * Doesn't look at field annotations which could cause issues if property names are changed
   * using annotations and there is an attempt to read jackson generated json back into SecurityUser
   * object. That is not currently the case for all field names.
   * Uses a custom comparator for declared field ordering to be used in example-users.json
   * @return this as a map
   */
  public Map<String, Object> toMap() {
    Map<String, Object> result = new TreeMap<>(new SecurityUserExampleDataComparator());
    Arrays.stream(this.getClass().getDeclaredFields()).forEach(field -> {
      log.info(() -> "Retrieving field " + field);
      field.setAccessible(true);
      Object value = ReflectionUtils.getField(field, this);
      if (value instanceof ParameterizedMessageFactory || value instanceof Logger) {
        log.info(() -> "Not including field " + field.getName() + " in user info map");
      } else {
        result.put(field.getName(), value);
      }
    });
    return result;
  }

  /**
   * A comparator for quality of life ordering in the example-users.json output.
   * This relates to handling which allows SecurityUser#password to be write only for
   * security reasons but also to be output into the example-users.json for local
   * development & testing data.
   */
  protected static class SecurityUserExampleDataComparator implements Comparator<String> {

    /** A local static copy of {@link SecurityUser} declared fields */
    private static final Field[] fields = SecurityUser.class.getDeclaredFields();

    /** Prevents iterating through fields array for every field compared */
    private static final Map<String, Integer> FIELD_NAME_TO_DECLARED_ORDER_MAP = new HashMap<>();

    static {
      for (int i = 0; i < fields.length; i++) {
        FIELD_NAME_TO_DECLARED_ORDER_MAP.put(fields[i].getName(), i);
      }
    }

    /**
     * Returns a value based on the order of declared fields in {@link SecurityUser}.
     * @param fieldName1 the first field name to be compared.
     * @param fieldName2 the second field name to be compared.
     * @return
     *   1 if fieldName1 is declared before fieldName2
     *   1 if fieldName1 is declared after fieldName2
     *   0 if fieldName1 is declared at the same position as fieldName2 ( probably only for duplicate field name )
     *   sorts null last ( which should not happen )
     */
    @Override
    public int compare(String fieldName1, String fieldName2) {
      Integer position1 = FIELD_NAME_TO_DECLARED_ORDER_MAP.get(fieldName1);
      Integer position2 = FIELD_NAME_TO_DECLARED_ORDER_MAP.get(fieldName2);
      if (fieldName1 == null) {
        return 1;
      } else if (fieldName2 == null) {
        return -1;
      } else
        return position1.compareTo(position2);
    }
  }
}
