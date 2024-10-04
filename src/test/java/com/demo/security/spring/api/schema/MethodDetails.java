package com.demo.security.spring.api.schema;


import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpMethod;

@Builder
@Getter
@ToString
public class MethodDetails {

  private HttpMethod method;

  private List<String> tags;

  private String operationId;

  private RequestBody requestBody;

  private Map<Integer, Response> responses;

}
