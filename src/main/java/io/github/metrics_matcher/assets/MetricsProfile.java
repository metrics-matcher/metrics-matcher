package io.github.metrics_matcher.assets;

import lombok.Value;

import java.util.Map;

@Value(staticConstructor = "of")
public class MetricsProfile {
    String name;
    Map<String, Object> params;
    Map<String, Object> metrics;
}
