package com.demo.security.spring.repository;

import com.demo.security.spring.model.SecurityUser;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SecurityUserRepository extends PagingAndSortingRepository<SecurityUser, Long>, CrudRepository<SecurityUser, Long> {

  SecurityUser getSecurityUserByUsername(String username);

  boolean existsByUsernameIgnoreCase(String username);

  @Transactional
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query(value = "UPDATE SecurityUser u SET u.password = ?2 WHERE u.username = ?1")
  int updateUserPassword(String username, String password);

}
