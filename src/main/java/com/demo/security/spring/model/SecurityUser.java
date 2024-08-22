package com.demo.security.spring.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.ReflectionUtils;

@Entity
@Table(name = "security_users")
@Getter
@Setter
@ToString(exclude = {"password", "accounts", "loans", "cards"}) // some we don't want in logs and some can cause hibernate lazy initialization errors
@SequenceGenerator(name = "security_users_id_seq", sequenceName = "security_users_id_seq", allocationSize = 50, initialValue = 1)
@JsonInclude(Include.NON_EMPTY)
public class SecurityUser implements UserDetails {

  /** Max number of failed attempts allowed within a given time window or in a row before lockout */
  public static final int MAX_ALLOWED_FAILED_LOGIN_ATTEMPTS = 5;

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "security_users_id_seq")
  private Long id;

  @NotBlank
  @Column(name = "username", length = 100)
  private String username;

  @NotBlank
  @Column(name = "email", length = 100)
  private String email;

  @NotBlank
  @Column(name = "password", length = 500)
  @JsonProperty(access = Access.WRITE_ONLY)
  private String password;

  @Column(name = "user_type", length = 100)
  @Enumerated(EnumType.STRING)
  @NotNull
  private UserType userType = UserType.external;

  @NotBlank
  @Column(name = "user_role", length = 100)
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

  @Column(name = "failed_login_attempts")
  private int failedLoginAttempts = 0;

  @Column(name = "num_previous_lockouts")
  private int numPreviousLockouts = 0;

  private boolean locked;

  @Column(name = "locked_date")
  private ZonedDateTime lockedDate;

  @Column(name = "unlock_date")
  private ZonedDateTime unlockDate;

  @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  private List<SecurityAuthority> authorities;

  /* I added the below one to many fields for quality of life when generating example data files but
  removing them or making transient seems preferable since they are handled via separate repositories */

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE })
  private List<Account> accounts;

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE })
  private List<Loan> loans;

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE })
  private List<Card> cards;

  @Embedded
  private EntityControlDates controlDates = new EntityControlDates();

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  public void setAuthorities(List<SecurityAuthority> authorities) {
    authorities.forEach(auth -> { if (auth.getUser() == null) { auth.setUser(this); } });
    this.authorities = authorities;
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

  public void setAccounts(List<Account> accounts) {
    this.accounts = accounts;
    if (accounts != null) {
      accounts.stream().forEach(account -> account.setUser(this));
    }
  }

  public void setLoans(List<Loan> loans) {
    this.loans = loans;
    if (loans != null) {
      loans.stream().forEach(loan -> loan.setUser(this));
    }
  }

  public void setCards(List<Card> cards) {
    this.cards = cards;
    if (cards != null) {
      cards.stream().forEach(card -> card.setUser(this));
    }
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
    if (getFailedLoginAttempts() > MAX_ALLOWED_FAILED_LOGIN_ATTEMPTS) {
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
      field.setAccessible(true);
      result.put(field.getName(), ReflectionUtils.getField(field, this));
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
