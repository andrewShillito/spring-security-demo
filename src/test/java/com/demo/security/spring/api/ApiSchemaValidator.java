package com.demo.security.spring.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.demo.security.spring.api.schema.MethodDetails;
import com.demo.security.spring.api.schema.Path;
import com.demo.security.spring.api.schema.RequestBody;
import com.demo.security.spring.api.schema.Response;
import com.demo.security.spring.api.schema.Schema;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpMethod;

/**
 * A proof of concept for an open-api schema validator which can be improved
 */
@ToString
@Log4j2
public class ApiSchemaValidator {

  private final Map<String, Object> schema;

  @Getter
  private final Map<String, Path> paths = new HashMap<>();

  private static final String KEY_TAGS = "tags";

  private static final String KEY_OPERATION_ID = "operationId";

  private static final String KEY_REQUEST_BODY = "requestBody";

  private static final String KEY_REQUIRED = "required";

  private static final String KEY_RESPONSES = "responses";

  private static final String KEY_DESCRIPTION = "description";

  private static final String KEY_CONTENT = "content";

  private static final String KEY_SCHEMA = "schema";

  private static final String KEY_TYPE = "type";

  private static final String KEY_REF = "$ref";

  public ApiSchemaValidator(Map<String, Object> schema) {
    Preconditions.checkNotNull(schema);
    Preconditions.checkArgument(!schema.isEmpty());
    this.schema = schema;
    buildPaths();
  }

  public void validate(String path, HttpMethod method, String operationId, List<String> tags,
      boolean hasRequestBody, boolean requestBodyRequired, List<Pair<String, String>> requestContentInfo,
      Integer status, String description, List<Pair<String, String>> responseContentInfo) {
    hasPath(path);
    hasMethod(path, method);
    hasOperationId(path, method, operationId);
    if (tags != null && !tags.isEmpty()) {
      for (String tag : tags) {
        hasTag(path, method, tag);
      }
    }
    if (!hasRequestBody) {
      assertNull(getRequestBody(path, method));
    } else {
      hasRequestInfo(path, method, requestBodyRequired, requestContentInfo);
    }
    hasResponseInfo(path, method, status, description, responseContentInfo);
  }

  public Path getPath(String path) {
    return paths.get(path);
  }

  public void hasPath(String path) {
    assertNotNull(getPath(path));
  }

  public void hasNotGotPath(String path) {
    assertFalse(paths.containsKey(path));
  }

  public Map<HttpMethod, MethodDetails> getMethods(String path) {
    if (paths.get(path) != null) {
      return paths.get(path).getMethods();
    }
    return null;
  }

  public MethodDetails getMethod(String path, HttpMethod method) {
    var methods = getMethods(path);
    if (methods != null) {
      return methods.get(method);
    }
    return null;
  }

  public void hasMethod(String path, HttpMethod httpMethod) {
    assertNotNull(getMethod(path, httpMethod));
  }

  public void hasMethodCount(String path, int count) {
    var methods = getMethods(path);
    assertNotNull(methods);
    assertEquals(count, methods.size());
  }

  public void hasNotGotMethod(String path, HttpMethod httpMethod) {
    hasPath(path);
    assertFalse(paths.get(path).getMethods().containsKey(httpMethod));
  }

  public void hasOperationId(String path, HttpMethod httpMethod, String operationId) {
    hasMethod(path, httpMethod);
    MethodDetails methodDetails = getMethod(path, httpMethod);
    assertNotNull(methodDetails);
    assertEquals(methodDetails.getOperationId(), operationId);
  }

  public void hasTag(String path, HttpMethod method, String tag) {
    List<String> tags = getTags(path, method);
    assertNotNull(tags);
    assertFalse(tags.isEmpty());
    assertTrue(tags.contains(tag), "Expected path " + path + " method " + method + " to have tag " + tag + "! Has tags " + tags);
  }

  public List<String> getTags(String path, HttpMethod method) {
    MethodDetails methodDetails = getMethod(path, method);
    if (methodDetails != null) {
      return methodDetails.getTags();
    }
    return null;
  }

  public void hasResponseStatus(String path, HttpMethod method, Integer status) {
    Map<Integer, Response> responses = getResponses(path, method);
    assertNotNull(responses);
    assertFalse(responses.isEmpty());
    assertTrue(responses.containsKey(status), "Expected path " + path + " method " + method + " to have status " + status + "! Has responses " + responses);
  }

  public RequestBody getRequestBody(String path, HttpMethod method) {
    MethodDetails methodDetails = getMethod(path, method);
    if (methodDetails != null) {
      return methodDetails.getRequestBody();
    }
    return null;
  }

  public void hasRequestInfo(String path, HttpMethod method, boolean required, List<Pair<String, String>> contentInfo) {
    RequestBody requestBody = getRequestBody(path, method);
    assertNotNull(requestBody);
    assertEquals(required, requestBody.isRequired());
    List<Pair<String, Schema>> actualContentInfo = requestBody.getContentInfo();
    assertNotNull(actualContentInfo);
    assertFalse(actualContentInfo.isEmpty());
    hasContentInfo(contentInfo, actualContentInfo);
  }

  public Map<Integer, Response> getResponses(String path, HttpMethod method) {
    MethodDetails methodDetails = getMethod(path, method);
    if (methodDetails != null) {
      return methodDetails.getResponses();
    }
    return null;
  }

  public void hasResponseInfo(String path, HttpMethod method, Integer status, String description,
      List<Pair<String, String>> contentInfo) {
    Map<Integer, Response> responses = getResponses(path, method);
    assertNotNull(responses);
    assertFalse(responses.isEmpty());
    assertTrue(responses.containsKey(status), "Expected path " + path + " method " + method + " to have status " + status + " but has " + responses);
    assertEquals(description, responses.get(status).getDescription(), "Expected path "
        + path + " method " + method + " status " + status + " to have description " + description + " but did not! Responses: " + responses);
    List<Pair<String, Schema>> schemas = responses.get(status).getContentInfo();
    assertNotNull(schemas);
    assertFalse(schemas.isEmpty());
    hasContentInfo(contentInfo, schemas);
  }

  private void hasContentInfo(List<Pair<String, String>> expected, List<Pair<String, Schema>> actual) {
    for (Pair<String, String> content : expected) {
      boolean located = false;
      for (Pair<String, Schema> actualContentInfo : actual) {
        if (content.getKey().equals(actualContentInfo.getKey())) {
          located = true;
          assertNotNull(actualContentInfo.getValue());
          assertNotNull(actualContentInfo.getValue().getType());
          assertTrue(
              content.getValue().equals(actualContentInfo.getValue().getType())
                  || actualContentInfo.getValue().getType().endsWith(content.getValue()),
              "Expected actualContentInfo " + actualContentInfo + " to match or end with " + content.getValue()
          );
          break;
        }
      }
      assertTrue(located, "Expected to find schema matching " + content.getValue() + " with type value " + content.getValue());
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> getSchemaPaths() {
    assertInstanceOf(Map.class, schema.get("paths"));
    return (Map<String, Object>) schema.get("paths");
  }

  private void buildPaths() {
    Map<String, Object> schemaPaths = getSchemaPaths();
    schemaPaths.forEach((key, val) -> {
      Path path = Path.builder()
          .route(key)
          .methods(buildMethods(val))
          .build();
      paths.put(key, path);
    });
  }

  private Map<HttpMethod, MethodDetails> buildMethods(Object value) {
    Map<HttpMethod, MethodDetails> result = new HashMap<>();
    if (value instanceof Map<?,?>) {
      for (Object key : ((Map<?, ?>) value).keySet()) {
        MethodDetails.MethodDetailsBuilder builder = MethodDetails.builder();
        HttpMethod method = Arrays.stream(HttpMethod.values())
            .filter(it -> it.name().toLowerCase().equals(key))
            .findFirst()
            .orElse(null);
        if (method == null) {
          log.error("Unable to locate http method matching " + key);
          continue;
        }
        builder.method(method);
        if (((Map<?, ?> ) value).get(key) instanceof Map<?, ?> val) {
          // handle tags, operationId, responses
          builder.tags(buildTags(val));
          if (val.get(KEY_OPERATION_ID) instanceof String operationId) {
            builder.operationId(operationId);
          }
          if (val.get(KEY_RESPONSES) instanceof Map<?, ?> responseMap) {
            builder.responses(buildResponses(responseMap));
          }
          if (val.get(KEY_REQUEST_BODY) instanceof Map<?, ?> requestBodyMap) {
            builder.requestBody(buildRequestBody(requestBodyMap));
          }
          result.put(method, builder.build());
        } else {
          log.error(() -> "Expected key " + key + " to be instanceof string");
        }
      }
    } else {
      log.error(() -> "Failed to turn value " + value + " into methodDetails map");
    }
    return result;
  }

  private List<String> buildTags(Map<?, ?> map) {
    List<String> tags = new ArrayList<>();
    if (map.get(KEY_TAGS) instanceof List<?> tagList) {
      tagList.forEach(it -> {
        if (it instanceof String) {
          tags.add((String) it);
        }
      });
    }
    return tags;
  }

  private Map<Integer, Response> buildResponses(Map<?, ?> responseMap) {
    Map<Integer, Response> result = new HashMap<>();
    for (Entry<?, ?> entry : responseMap.entrySet()) {
      if (entry.getKey() instanceof String) {
        try {
          Integer statusCode = Integer.parseInt((String) entry.getKey());
          Response.ResponseBuilder builder = Response.builder();
          if (entry.getValue() instanceof Map<?, ?> map) {
            if (map.get(KEY_DESCRIPTION) instanceof String description) {
              builder.description(description);
            }
            if (map.get(KEY_CONTENT) instanceof Map<?, ?> contentMap) {
              List<Pair<String, Schema>> contentInfo = getContentInfo(contentMap);
              if (!contentInfo.isEmpty()) {
                builder.contentInfo(contentInfo);
              }
            }
          }
          result.put(statusCode, builder.build());
        } catch (NumberFormatException e) {
          log.error("Failed to determine http status code", e);
        }
      }
    }
    return result;
  }

  private RequestBody buildRequestBody(Map<?, ?> requestMap) {
    RequestBody.RequestBodyBuilder builder = RequestBody.builder();
    if (requestMap.get(KEY_REQUIRED) instanceof Boolean required) {
      builder.required(required);
    }
    if (requestMap.get(KEY_CONTENT) instanceof Map<?, ?> contentMap) {
      List<Pair<String, Schema>> contentInfo = getContentInfo(contentMap);
      if (!contentInfo.isEmpty()) {
        builder.contentInfo(contentInfo);
      }
    }
    return builder.build();
  }

  private List<Pair<String, Schema>> getContentInfo(Map<?, ?> contentMap) {
    List<Pair<String, Schema>> pairs = new ArrayList<>();
    if (contentMap != null) {
      for (Entry<?, ?> entry : contentMap.entrySet()) {
        if (entry.getKey() instanceof String key && entry.getValue() instanceof Map<?, ?> schemaMap) {
          if (schemaMap.get(KEY_SCHEMA) instanceof Map<?, ?> val) {
            Schema schema = buildSchema(val);
            if (schema != null) {
              pairs.add(new ImmutablePair<>(key, schema));
            }
          }
        }
      }
    }
    return pairs;
  }

  private Schema buildSchema(Map<?, ?> schemaMap) {
    if (schemaMap != null) {
      if (schemaMap.get(KEY_TYPE) instanceof String type) {
        return Schema.builder().type(type).build();
      } else if (schemaMap.get(KEY_REF) instanceof String ref) {
        return Schema.builder().type(ref).build();
      }
    }
    return null;
  }
}
