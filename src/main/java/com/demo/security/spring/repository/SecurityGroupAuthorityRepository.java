package com.demo.security.spring.repository;

import com.demo.security.spring.model.remove.SecurityGroupAuthority;
import com.demo.security.spring.model.remove.SecurityGroupAuthorityKey;
import org.springframework.data.repository.CrudRepository;

public interface SecurityGroupAuthorityRepository extends CrudRepository<SecurityGroupAuthority, SecurityGroupAuthorityKey> {

}
