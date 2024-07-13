package com.demo.security.spring.repository;

import com.demo.security.spring.model.NoticeDetails;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface NoticeDetailsRepository extends CrudRepository<NoticeDetails, Long> {

  @Query(value = "SELECT n FROM NoticeDetails n WHERE current date BETWEEN n.entityStartAndEndDates.startDate AND n.entityStartAndEndDates.endDate")
  List<NoticeDetails> getAllActiveNotices();

}
