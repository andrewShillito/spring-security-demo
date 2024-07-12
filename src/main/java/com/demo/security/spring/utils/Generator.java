package com.demo.security.spring.utils;

import java.util.Collection;

public interface Generator<T> {

  /**
   * Primary entry-point for all generators
   */
  T generate();

}
