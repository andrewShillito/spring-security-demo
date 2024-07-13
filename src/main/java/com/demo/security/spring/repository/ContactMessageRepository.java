package com.demo.security.spring.repository;

import com.demo.security.spring.model.ContactMessage;
import org.springframework.data.repository.CrudRepository;

public interface ContactMessageRepository extends CrudRepository<ContactMessage, Long> {

}
