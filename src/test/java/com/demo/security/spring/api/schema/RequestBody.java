package com.demo.security.spring.api.schema;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.tuple.Pair;

@Builder
@Getter
@ToString
public class RequestBody {

  private boolean required;

  private List<Pair<String, Schema>> contentInfo;

}
