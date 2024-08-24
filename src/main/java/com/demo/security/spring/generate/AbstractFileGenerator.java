package com.demo.security.spring.generate;

import com.demo.security.spring.model.EntityStartAndEndDates;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Collection;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.datafaker.Faker;
import org.apache.commons.lang3.StringUtils;

@Log4j2
public abstract class AbstractFileGenerator extends AbstractGenerator<Collection<?>> implements Writer {

  protected static final String DEFAULT_OUTPUT_DIRECTORY = "./src/main/resources/seed/";

  protected final boolean overwriteFiles;

  @Getter
  protected final File outputFile;

  public AbstractFileGenerator(Faker faker, ObjectMapper objectMapper, String fileName) {
    this(faker, objectMapper, DEFAULT_OUTPUT_DIRECTORY, fileName, true);
  }

  public AbstractFileGenerator(Faker faker, ObjectMapper objectMapper, String outputFileDir, String fileName, boolean overwriteFiles) {
    super(faker, objectMapper);
    Preconditions.checkArgument(StringUtils.isNotBlank(outputFileDir), "File path cannot be empty");
    validateArgs(outputFileDir, fileName);
    this.overwriteFiles = overwriteFiles;
    outputFile = new File(outputFileDir + fileName);
    initOutputFile(outputFile);
  }

  void validateArgs(String fileDir, String fileName) {
    Preconditions.checkArgument(StringUtils.isNotBlank(fileDir), "File output directory cannot be empty");
    Preconditions.checkArgument(StringUtils.isNotBlank(fileName), "File output name cannot be empty");
  }

  @Override
  public void write() {
    write(generate());
  }

  @Override
  public void write(Collection<?> generated) {
    try {
      if (generated != null && !generated.isEmpty()) {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, generated);
      } else {
        log.warn(() -> "Generation returned empty collection");
      }
      log.info("Wrote " + generated.size() + " items to " + outputFile.getPath());
    } catch (IOException e) {
      throw new RuntimeException("Failed data generation for file " + outputFile.getPath(), e);
    }
  }

  public String getOutputFileName() {
    return outputFile != null ? outputFile.getName() : null;
  }

  protected void initOutputFile(final File file) {
    Preconditions.checkNotNull(file, "Output file cannot be null");
    if (!file.exists() || overwriteFiles) {
      try {
        if (!file.createNewFile() && !file.exists()) {
          log.error("Failed to create file " + file.getPath());
        }
      } catch (IOException e) {
        log.error(() -> "Failed to initialize output file with path " + file.getPath());
      }
    }
    Preconditions.checkArgument(file.exists(), "Output file does not exist " + file + " - current dir " + System.getProperty("user.dir"));
    Preconditions.checkArgument(file.canWrite(), "Write permissions to file ${file} are required in order to run user generation");
  }

  public EntityStartAndEndDates currentDate() {
    EntityStartAndEndDates startAndEndDates = new EntityStartAndEndDates();
    startAndEndDates.setStartDate(ZonedDateTime.now().minusDays(faker.random().nextInt(1, 365)));
    startAndEndDates.setEndDate(ZonedDateTime.now().plusDays(faker.random().nextInt(1, 365)));
    return startAndEndDates;
  }

  public EntityStartAndEndDates futureDate() {
    EntityStartAndEndDates startAndEndDates = new EntityStartAndEndDates();
    startAndEndDates.setStartDate(ZonedDateTime.now().plusDays(faker.random().nextInt(1, 365)));
    startAndEndDates.setEndDate(ZonedDateTime.now().plusDays(faker.random().nextInt(365, 730)));
    return startAndEndDates;
  }


  public EntityStartAndEndDates pastDate() {
    EntityStartAndEndDates startAndEndDates = new EntityStartAndEndDates();
    startAndEndDates.setStartDate(ZonedDateTime.now().minusDays(faker.random().nextInt(365, 730)));
    startAndEndDates.setEndDate(ZonedDateTime.now().minusDays(faker.random().nextInt(1, 365)));
    return startAndEndDates;
  }
}
