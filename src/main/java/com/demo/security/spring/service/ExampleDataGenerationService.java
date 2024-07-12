package com.demo.security.spring.service;

import com.demo.security.spring.generation.ContactMessagesFileGenerator;
import com.demo.security.spring.generation.NoticeDetailsFileGenerator;
import com.demo.security.spring.generation.UserFileGenerator;
import com.demo.security.spring.model.ContactMessage;
import com.demo.security.spring.model.NoticeDetails;
import com.demo.security.spring.model.SecurityUser;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Builder
@ToString
@Getter
public class ExampleDataGenerationService {

  private UserFileGenerator userFileGenerator;

  private NoticeDetailsFileGenerator noticeDetailsFileGenerator;

  private ContactMessagesFileGenerator contactMessagesFileGenerator;

  public List<SecurityUser> generateUsers(boolean writeToFile) {
    final List<SecurityUser> generatedUsers = userFileGenerator.generate();
    if (writeToFile) {
      userFileGenerator.write(generatedUsers);
    }
    return generatedUsers;
  }

  public List<NoticeDetails> generateNotices(boolean writeToFile) {
    final List<NoticeDetails> noticeDetails = noticeDetailsFileGenerator.generate();
    if (writeToFile) {
      noticeDetailsFileGenerator.write(noticeDetails);
    }
    return noticeDetails;
  }

  public List<ContactMessage> generateMessages(boolean writeToFile) {
    final List<ContactMessage> contactMessages = contactMessagesFileGenerator.generate();
    if (writeToFile) {
      contactMessagesFileGenerator.write(contactMessages);
    }
    return contactMessages;
  }
}
