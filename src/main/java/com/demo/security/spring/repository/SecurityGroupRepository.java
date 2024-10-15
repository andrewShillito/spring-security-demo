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

  @Query(value = "SELECT g FROM SecurityGroup g JOIN SecurityGroupAuthority j ON j.group.id = g.id LEFT JOIN FETCH SecurityAuthority a ON a.id = j.authority.id WHERE g.code IN ?1")
  Set<SecurityGroup> retrieveAllByCode(Collection<String> code);

//  Set<SecurityGroup> findAllByUsersIsContaining(SecurityUser user);

}
