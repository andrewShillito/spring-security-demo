package com.demo.security.spring.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

@Getter
@Setter
@JsonPropertyOrder(value = {"id", "role"})
public class AuthorityCreationResponse {

  private String role;

  private Long id;

  public AuthorityCreationResponse() {
  }

  public AuthorityCreationResponse(String role, Long id) {
    this.role = role;
    this.id = id;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static AuthorityCreationResponse fromAuthority(GrantedAuthority authority) {
    if (authority instanceof SecurityAuthority securityAuthority) {
      return AuthorityCreationResponse.builder()
          .id(securityAuthority.getId())
          .role(securityAuthority.getAuthority())
          .build();
    } else {
      throw new IllegalArgumentException(
          "Authority implementations other than SimpleGrantedAuthority are not supported.");
    }
  }

  public static class Builder {

    private AuthorityCreationResponse authorityCreationResponse = new AuthorityCreationResponse();

    public Builder clear() {
      authorityCreationResponse = new AuthorityCreationResponse();
      return this;
    }

    public AuthorityCreationResponse build() {
      return authorityCreationResponse;
    }

    public Builder role(String role) {
      authorityCreationResponse.setRole(role);
      return this;
    }

    public Builder id(Long id) {
      authorityCreationResponse.setId(id);
      return this;
    }
  }
}
