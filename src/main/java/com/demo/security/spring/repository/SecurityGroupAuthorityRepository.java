package com.demo.security.spring.repository;

import com.demo.security.spring.model.SecurityGroupAuthority;
import com.demo.security.spring.model.SecurityGroupAuthorityKey;
import org.springframework.data.repository.CrudRepository;

public interface SecurityGroupAuthorityRepository extends CrudRepository<SecurityGroupAuthority, SecurityGroupAuthorityKey> {

}
