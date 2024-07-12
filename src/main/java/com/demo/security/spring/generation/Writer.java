package com.demo.security.spring.generation;

import java.util.Collection;

public interface Writer {

  /**
   * Primary entry-point for all writers
   */
  void write();

  /**
   * Write without re-generating
   * @param generated the generated data
   */
  void write(Collection<?> generated);

}
