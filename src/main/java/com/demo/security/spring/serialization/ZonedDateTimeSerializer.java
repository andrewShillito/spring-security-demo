package com.demo.security.spring.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import lombok.Setter;

@Setter
public class ZonedDateTimeSerializer extends StdSerializer<ZonedDateTime> {

  public ZonedDateTimeSerializer(Class<ZonedDateTime> t) {
    super(t);
  }

  public ZonedDateTimeSerializer(JavaType type) {
    super(type);
  }

  public ZonedDateTimeSerializer(Class<?> t, boolean dummy) {
    super(t, dummy);
  }

  public ZonedDateTimeSerializer(StdSerializer<?> src) {
    super(src);
  }

  @Override
  public void serialize(ZonedDateTime value, JsonGenerator gen, SerializerProvider provider)
      throws IOException {
    if (value != null) {
      gen.writeString(DateTimeFormatter.ISO_ZONED_DATE_TIME.format(value));
    }
  }
}
