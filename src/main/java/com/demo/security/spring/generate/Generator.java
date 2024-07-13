package com.demo.security.spring.generate;

public interface Generator<T> {

  /**
   * Primary entry-point for all generators
   * @return T
   */
  T generate();

  /**
   * For generating a specific number of items
   * @param count the number of items to generate
   * @return T
   */
  T generate(int count);

}
