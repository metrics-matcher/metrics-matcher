package io.github.metrics_matcher.xyz;

import lombok.Value;

import java.util.Map;

@Value
public class MetricsProfile {
    String name;
    Map<String, Object> params;
    Map<String, Object> metrics;
}
