package com.demo.security.spring.repository;

import com.demo.security.spring.model.SecurityGroup;
import com.demo.security.spring.model.SecurityUser;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface SecurityGroupRepository extends CrudRepository<SecurityGroup, Long> {

  SecurityGroup getSecurityGroupByCode(String code);

  Set<SecurityGroup> findAllByCodeIn(Collection<String> code);

}
