package com.demo.security.spring.utils;

import com.google.common.base.Preconditions;
import java.util.Collection;

public class ValidationUtils {

  public static void notEmpty(Collection<?> collection) {
    notEmpty(collection, "Collection cannot be empty");
  }

  public static void notEmpty(Collection<?> collection, String message) {
    Preconditions.checkArgument(collection != null && !collection.isEmpty(), message);
  }

}
