package io.github.metrics_matcher.assets;

import lombok.Value;

@Value(staticConstructor = "of")
public class Query {
    String id;
    String title;
    String sql;
}
