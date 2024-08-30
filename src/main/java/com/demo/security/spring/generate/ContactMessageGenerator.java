package com.demo.security.spring.generate;

import com.demo.security.spring.model.ContactMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import net.datafaker.Faker;

@Log4j2
public class ContactMessageGenerator extends AbstractGenerator<List<ContactMessage>> {

  public static final String DEFAULT_OUTPUT_FILE = "example-contact-messages.json";

  public ContactMessageGenerator(Faker faker,
      ObjectMapper objectMapper) {
    this(faker, objectMapper, DEFAULT_ITEM_COUNT);
  }

  public ContactMessageGenerator(Faker faker, ObjectMapper objectMapper, int itemCount) {
    super(faker, objectMapper, itemCount);
  }

  @Override
  public List<ContactMessage> generate() {
    return generate(getItemCount());
  }

  @Override
  public List<ContactMessage> generate(int count) {
    final List<ContactMessage> contactMessages = new ArrayList<>();
    for (int i = 0; i < count; i++) {
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
    contactMessage.setCreated(randomPastDate());
    return contactMessage;
  }
}
