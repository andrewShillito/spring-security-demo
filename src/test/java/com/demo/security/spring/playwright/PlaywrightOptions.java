package com.demo.security.spring.playwright;

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.Browser.NewContextOptions;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class PlaywrightOptions implements OptionsFactory {

  @Override
  public Options getOptions() {
    return new Options().setHeadless(PlaywrightUtils.isHeadless())
        .setContextOptions(new NewContextOptions().setBaseURL(PlaywrightUtils.appPathFull()))
        .setApiRequestOptions(new APIRequest.NewContextOptions().setBaseURL(PlaywrightUtils.appPathFull()));
  }
}
