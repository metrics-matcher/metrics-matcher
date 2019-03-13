package io.github.metrics_matcher.dto;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(of = "id")
public class Query {
    private String id;
    private String title;
    private String sql;
}
