package com.demo.security.spring.repository;

import com.demo.security.spring.model.AuthenticationAttempt;
import java.time.ZonedDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * A repository for interacting with the {@link AuthenticationAttempt} objects stored for given userId or logon
 * provided usernames.
 */
public interface AuthenticationAttemptRepository extends CrudRepository<AuthenticationAttempt, Long> {

  @Query(value = "SELECT a FROM AuthenticationAttempt a WHERE a.userId = ?1 ORDER BY a.attemptTime DESC")
  List<AuthenticationAttempt> findAllByUserId(Long userId);

  @Query(value = "SELECT a FROM AuthenticationAttempt a WHERE a.username = ?1 ORDER BY a.attemptTime DESC")
  List<AuthenticationAttempt> findAllByUsername(String username);

  @Query(value = "SELECT a FROM AuthenticationAttempt a WHERE a.userId = ?1 AND a.attemptTime > ?2 ORDER BY a.attemptTime DESC")
  List<AuthenticationAttempt> findAllByUserIdAndAttemptTimeAfter(Long userId, ZonedDateTime after);

  @Query(value = "SELECT a FROM AuthenticationAttempt a WHERE a.username = ?1 AND a.attemptTime > ?2 ORDER BY a.attemptTime DESC")
  List<AuthenticationAttempt> findAllByUsernameAndAttemptTimeAfter(String username, ZonedDateTime after);


}
