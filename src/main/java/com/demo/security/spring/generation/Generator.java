package com.demo.security.spring.generation;

public interface Generator<T> {

  /**
   * Primary entry-point for all generators
   */
  T generate();

}
