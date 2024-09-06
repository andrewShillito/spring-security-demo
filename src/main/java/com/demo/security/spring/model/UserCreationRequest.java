package com.demo.security.spring.model;

import com.demo.security.spring.validation.IsValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@EqualsAndHashCode
@ToString
public class UserCreationRequest {

  @NotBlank
  @Size(min = 8, max = 100)
  private String username;

  @IsValidPassword
  private String password;

  @NotBlank
  @Email
  private String email;

  public SecurityUser toSecurityUser() {
    final SecurityUser user = new SecurityUser();
    user.setPassword(password);
    user.setUsername(username);
    user.setUserType(UserType.external);
    user.setUserRole("STANDARD");
    user.setEmail(email);
    user.setEnabled(true);

    // existing basic auth roles which will be flushed out more soon
    List<SecurityAuthority> authorities = new ArrayList<>();
    SecurityAuthority authority = new SecurityAuthority();
    authority.setAuthority("ROLE_USER");
    authorities.add(authority);
    user.setAuthorities(authorities);

    return user;
  }

}
