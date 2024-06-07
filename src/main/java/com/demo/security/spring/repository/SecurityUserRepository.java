package com.demo.security.spring.repository;

import com.demo.security.spring.model.SecurityUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityUserRepository extends CrudRepository<SecurityUser, Long> {

  SecurityUser getSecurityUserByUsername(String username);

}
