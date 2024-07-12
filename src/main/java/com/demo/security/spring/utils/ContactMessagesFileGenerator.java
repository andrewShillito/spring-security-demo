package com.demo.security.spring.utils;

import com.demo.security.spring.model.ContactMessage;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ContactMessagesFileGenerator extends AbstractFileGenerator {

  public ContactMessagesFileGenerator(String fileName) {
    super(fileName);
  }

  public ContactMessagesFileGenerator(String outputFileDir, String fileName) {
    super(outputFileDir, fileName);
  }

  public ContactMessagesFileGenerator(String outputFileDir, String fileName, boolean overwriteFiles) {
    super(outputFileDir, fileName, overwriteFiles);
  }

  @Override
  public List<ContactMessage> generate() {
    final List<ContactMessage> contactMessages = new ArrayList<>();
    for (int i = 0; i < getItemCount(); i++) {
      contactMessages.add(generateContactMessage());
    }
    log.info(() -> "Generated " + contactMessages.size() + " contactMessages");
    return contactMessages;
  }

  private ContactMessage generateContactMessage() {
    final ContactMessage contactMessage = new ContactMessage();
    contactMessage.setContactEmail(faker.internet().emailAddress());
    contactMessage.setContactName(faker.name().fullName());
    contactMessage.setSubject(faker.lorem().sentence());
    contactMessage.setMessage(faker.lorem().paragraph());
    contactMessage.setCreatedDate(randomPastDate());
    return contactMessage;
  }

  @Override
  public ContactMessagesFileGenerator setItemCount(int itemCount) {
    return (ContactMessagesFileGenerator) super.setItemCount(itemCount);
  }
}
