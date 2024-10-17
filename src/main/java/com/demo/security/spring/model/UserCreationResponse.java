package com.demo.security.spring.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonPropertyOrder(value = {"id", "username"})
public class UserCreationResponse {

  private Long id;

  private String username;

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private UserCreationResponse userCreationResponse = new UserCreationResponse();

    public Builder clear() {
      userCreationResponse = new UserCreationResponse();
      return this;
    }

    public UserCreationResponse build() {
      return userCreationResponse;
    }

    public Builder id(Long id) {
      userCreationResponse.setId(id);
      return this;
    }

    public Builder username(String username) {
      userCreationResponse.setUsername(username);
      return this;
    }
  }

}
