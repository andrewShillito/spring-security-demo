package com.demo.security.spring.repository;

import com.demo.security.spring.model.SecurityUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityUserRepository extends PagingAndSortingRepository<SecurityUser, Long>, CrudRepository<SecurityUser, Long> {

  SecurityUser getSecurityUserByUsername(String username);

}
