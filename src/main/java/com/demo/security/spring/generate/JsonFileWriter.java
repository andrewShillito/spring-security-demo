package com.demo.security.spring.generate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class JsonFileWriter<T> implements Writer {

  @Getter
  @Setter
  private ObjectMapper objectMapper;

  private String outputFilePath;

  @Getter
  private T toWrite;

  public static final String DEFAULT_OUTPUT_DIRECTORY = "./src/main/resources/seed/";

  public JsonFileWriter(ObjectMapper objectMapper) {
    Preconditions.checkNotNull(objectMapper);
    this.objectMapper = objectMapper;
  }

  public JsonFileWriter(ObjectMapper objectMapper, String outputFilePath) {
    this(objectMapper);
    Preconditions.checkNotNull(outputFilePath);
    this.outputFilePath = outputFilePath;
  }

  public JsonFileWriter(ObjectMapper objectMapper, String outputFilePath, T toWrite) {
    this(objectMapper, outputFilePath);
    Preconditions.checkNotNull(toWrite);
    this.toWrite = toWrite;
  }

  public JsonFileWriter<T> setToWrite(T toWrite) {
    Preconditions.checkNotNull(toWrite);
    this.toWrite = toWrite;
    return this;
  }

  @Override
  public void write() {
    final File file = new File(outputFilePath);
    try {
      if (toWrite != null) {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, toWrite);
      } else {
        log.warn(() -> "Generation returned empty collection");
      }
      if (toWrite instanceof Collection<?>) {
        log.info("Wrote " + ((Collection<?>) toWrite).size() + " items to " + outputFilePath);
      } else {
        log.info("Wrote item of type " + toWrite.getClass().getName() + " to " + outputFilePath);
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed data generation for file " + outputFilePath, e);
    }
  }
}
