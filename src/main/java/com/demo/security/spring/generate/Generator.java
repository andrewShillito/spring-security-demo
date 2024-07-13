package com.demo.security.spring.generate;

public interface Generator<T> {

  /**
   * Primary entry-point for all generators
   */
  T generate();

}
