package com.demo.security.spring.generate;

import com.demo.security.spring.model.NoticeDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import net.datafaker.Faker;

@Log4j2
public class NoticeDetailsGenerator extends AbstractGenerator<List<NoticeDetails>> {

  public NoticeDetailsGenerator(Faker faker,
      ObjectMapper objectMapper) {
    this(faker, objectMapper, DEFAULT_ITEM_COUNT);
  }

  public NoticeDetailsGenerator(Faker faker, ObjectMapper objectMapper, int itemCount) {
    super(faker, objectMapper, itemCount);
  }

  @Override
  public List<NoticeDetails> generate() {
    return generate(getItemCount());
  }

  @Override
  public List<NoticeDetails> generate(int count) {
    final List<NoticeDetails> noticeDetails = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      if ((i % 3) == 0) {
        noticeDetails.add(generateCurrentNotice());
      } else if (i % 3 == 1) {
        noticeDetails.add(generatePastNotice());
      } else {
        noticeDetails.add(generateFutureNotice());
      }
    }
    log.info(() -> "Generated " + noticeDetails.size() + " noticeDetails");
    return noticeDetails;
  }

  public NoticeDetails generateNotice() {
    final NoticeDetails notice = new NoticeDetails();
    notice.setNoticeDetails(faker.lorem().paragraph());
    notice.setNoticeSummary(faker.lorem().sentence());
    notice.setControlDates(randomEntityControlDates());
    notice.setEntityStartAndEndDates(randomEntityStartAndEndDates());
    return notice;
  }

  public NoticeDetails generateCurrentNotice() {
    NoticeDetails noticeDetails = generateNotice();
    noticeDetails.setEntityStartAndEndDates(currentDate());
    return noticeDetails;
  }

  public NoticeDetails generateFutureNotice() {
    NoticeDetails noticeDetails = generateNotice();
    noticeDetails.setEntityStartAndEndDates(futureDate());
    return noticeDetails;
  }

  public NoticeDetails generatePastNotice() {
    NoticeDetails noticeDetails = generateNotice();
    noticeDetails.setEntityStartAndEndDates(pastDate());
    return noticeDetails;
  }
}
