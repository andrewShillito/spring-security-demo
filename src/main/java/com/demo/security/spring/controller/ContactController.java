package com.demo.security.spring.controller;

import com.demo.security.spring.error.ValidationErrorUtils;
import com.demo.security.spring.model.ContactMessage;
import com.demo.security.spring.repository.ContactMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log4j2
public class ContactController {

  public static final String RESOURCE_PATH = "/contact";

  private ContactMessageRepository contactMessageRepository;

  private ObjectMapper objectMapper;

  @Autowired
  public void setContactMessageRepository(
      ContactMessageRepository contactMessageRepository) {
    this.contactMessageRepository = contactMessageRepository;
  }

  @Autowired
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @PostMapping(value = RESOURCE_PATH, produces = "application/json")
  public ResponseEntity<String> createContactMessage(
      @Valid @RequestBody final ContactMessage contactMessage,
      final BindingResult bindingResult) throws IOException
  {
    ResponseEntity<String> response = null;
    if (!bindingResult.hasErrors()) {
      ContactMessage saved = null;
      try {
        saved = contactMessageRepository.save(contactMessage);
      } catch (Exception e) {
        log.error(() -> "Failed to create contact message", e);
        response = ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(Map.of("message", "Failed to create message")));
      }
      if (saved != null) {
        try {
          response = ResponseEntity
              .status(HttpStatus.CREATED)
              .body(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(saved));
        } catch (IOException e) {
          response = ResponseEntity
              .status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body("Created contact message and encountered exception afterward");
        }
      }
    } else {
      try {
        final String body = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
            ValidationErrorUtils.generateErrorDetails(bindingResult));
        log.info(() -> "Rejected contact message request which was invalid\n" + body);
        response = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
      } catch (IOException e) {
        log.error(() -> "Failed to return error details to client for contact message", e);
        response = ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(Map.of("message", "Internal server error")));
      }
    }
    return response;
  }
}
