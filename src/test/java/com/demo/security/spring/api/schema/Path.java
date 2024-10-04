package com.demo.security.spring.api.schema;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpMethod;

@Builder
@Getter
@ToString
public class Path {

  private String route;

  private Map<HttpMethod, MethodDetails> methods;

}
