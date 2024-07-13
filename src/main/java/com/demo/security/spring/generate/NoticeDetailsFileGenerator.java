package com.demo.security.spring.generate;

import com.demo.security.spring.model.NoticeDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import net.datafaker.Faker;

@Log4j2
public class NoticeDetailsFileGenerator extends AbstractFileGenerator {

  public static final String DEFAULT_FILE_NAME = "example-notice-details.json";

  public NoticeDetailsFileGenerator(Faker faker,
      ObjectMapper objectMapper) {
    this(faker, objectMapper, DEFAULT_FILE_NAME);
  }

  public NoticeDetailsFileGenerator(Faker faker,
      ObjectMapper objectMapper, String fileName) {
    super(faker, objectMapper, fileName);
  }

  public NoticeDetailsFileGenerator(Faker faker,
      ObjectMapper objectMapper, String outputFileDir, String fileName,
      boolean overwriteFiles) {
    super(faker, objectMapper, outputFileDir, fileName, overwriteFiles);
  }

  @Override
  public List<NoticeDetails> generate() {
    final List<NoticeDetails> noticeDetails = new ArrayList<>();
    for (int i = 0; i < getItemCount(); i++) {
      noticeDetails.add(generateNotice());
    }
    log.info(() -> "Generated " + noticeDetails.size() + " noticeDetails");
    return noticeDetails;
  }

  private NoticeDetails generateNotice() {
    final NoticeDetails notice = new NoticeDetails();
    notice.setNoticeDetails(faker.lorem().paragraph());
    notice.setNoticeSummary(faker.lorem().sentence());
    notice.setControlDates(randomEntityControlDates());
    notice.setEntityStartAndEndDates(randomEntityStartAndEndDates());
    return notice;
  }
}
