package com.demo.security.spring.utils;

import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

@Log4j2
public abstract class AbstractFileGenerator extends AbstractGenerator<Collection<?>> implements Writer {

  protected static final String DEFAULT_OUTPUT_DIRECTORY = "./src/main/resources/seed/";

  protected final boolean overwriteFiles;

  protected final File outputFile;

  public AbstractFileGenerator(String fileName) {
    this(DEFAULT_OUTPUT_DIRECTORY, fileName);
  }

  public AbstractFileGenerator(String outputFileDir, String fileName) {
    this(outputFileDir, fileName, true);
  }

  public AbstractFileGenerator(String outputFileDir, String fileName, boolean overwriteFiles) {
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
    try {
      Collection<?> generated = generate();
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
    Preconditions.checkArgument(file.exists(), "Output file ${file} does not exist");
    Preconditions.checkArgument(file.canWrite(), "Write permissions to file ${file} are required in order to run user generation");
  }
}
