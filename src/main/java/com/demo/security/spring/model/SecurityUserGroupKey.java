package com.demo.security.spring.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
public class SecurityUserGroupKey implements Serializable {

  @Column(name = "user_id")
  @NotNull
  private Long userId;

  @Column(name = "group_id")
  @NotNull
  private Long groupId;


}
