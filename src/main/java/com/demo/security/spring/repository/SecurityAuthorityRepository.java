package com.demo.security.spring.repository;

import com.demo.security.spring.model.SecurityAuthority;
import com.demo.security.spring.model.SecurityUser;
import java.util.Collection;
import java.util.Set;
import org.springframework.data.repository.CrudRepository;

public interface SecurityAuthorityRepository extends CrudRepository<SecurityAuthority, Long> {

  Set<SecurityAuthority> findAllByAuthorityIn(Collection<String> authorities);

}
