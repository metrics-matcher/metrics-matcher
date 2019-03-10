package io.github.metrics_matcher.core;

import lombok.Value;

@Value(staticConstructor = "of")
public class Query {
    private String id;
    private String title;
    private String sql;
}
