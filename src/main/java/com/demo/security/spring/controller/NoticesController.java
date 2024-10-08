package com.demo.security.spring.controller;

import com.demo.security.spring.model.NoticeDetails;
import com.demo.security.spring.repository.NoticeDetailsRepository;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NoticesController {

  public static final String RESOURCE_PATH = "/notices";

  private NoticeDetailsRepository noticeDetailsRepository;

  @Autowired
  public void setNoticeDetailsRepository(
      NoticeDetailsRepository noticeDetailsRepository) {
    this.noticeDetailsRepository = noticeDetailsRepository;
  }

  @GetMapping(RESOURCE_PATH)
  public ResponseEntity<List<NoticeDetails>> getNotices() {
    final List<NoticeDetails> activeNotices = noticeDetailsRepository.getAllActiveNotices();
    if (activeNotices != null) {
      return ResponseEntity.ok().cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS))
          .body(activeNotices);
    } else {
      return null;
    }
  }
}
