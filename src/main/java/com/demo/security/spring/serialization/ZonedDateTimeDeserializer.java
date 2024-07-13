package com.demo.security.spring.serialization;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Setter
@Log4j2
public class ZonedDateTimeDeserializer extends StdDeserializer<ZonedDateTime> {

  public ZonedDateTimeDeserializer(Class<?> vc) {
    super(vc);
  }

  public ZonedDateTimeDeserializer(JavaType valueType) {
    super(valueType);
  }

  public ZonedDateTimeDeserializer(StdDeserializer<?> src) {
    super(src);
  }

  @Override
  public ZonedDateTime deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JacksonException {
    final String value = p.getValueAsString();
    if (value != null) {
      try {
        return DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(value, ZonedDateTime::from);
      } catch (DateTimeParseException e) {
        log.error(() -> "Failed to parse zonedDateTime " + value, e);
        throw new RuntimeException(e);
      }
    }
    return null;
  }
}
