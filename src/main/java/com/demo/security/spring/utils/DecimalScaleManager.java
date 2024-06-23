package com.demo.security.spring.utils;

import java.math.BigDecimal;

public class DecimalScaleManager {

  /**
   * Return a rounded and scaled big decimal based on global application settings
   * @param decimal
   * @return
   */
  public static BigDecimal setScale(BigDecimal decimal) {
    if (decimal != null) {
      decimal = decimal.setScale(Constants.GLOBAL_SCALE, Constants.GLOBAL_ROUNDING_MODE);
    }
    return decimal;
  }

}
