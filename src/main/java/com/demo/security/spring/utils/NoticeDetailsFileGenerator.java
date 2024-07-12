package com.demo.security.spring.utils;

import com.demo.security.spring.model.NoticeDetails;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class NoticeDetailsFileGenerator extends AbstractFileGenerator {

  public NoticeDetailsFileGenerator(String fileName) {
    super(fileName);
  }

  public NoticeDetailsFileGenerator(String outputFileDir, String fileName) {
    super(outputFileDir, fileName);
  }

  public NoticeDetailsFileGenerator(String outputFileDir, String fileName, boolean overwriteFiles) {
    super(outputFileDir, fileName, overwriteFiles);
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
