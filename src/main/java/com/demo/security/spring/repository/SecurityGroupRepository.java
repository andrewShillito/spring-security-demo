package com.demo.security.spring.repository;

import com.demo.security.spring.model.SecurityGroup;
import java.util.Collection;
import java.util.Set;
import org.springframework.data.repository.CrudRepository;

public interface SecurityGroupRepository extends CrudRepository<SecurityGroup, Long> {

  SecurityGroup getSecurityGroupByCode(String code);

  Set<SecurityGroup> findAllByCodeIn(Collection<String> code);

  SecurityGroup findByCodeEquals(String code);

}
