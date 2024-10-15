package com.demo.security.spring.model.remove;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
public class SecurityGroupAuthorityKey implements Serializable {

  @Column(name = "group_id")
  private Long groupId;

  @Column(name = "authority_id")
  private Long authorityId;
}
