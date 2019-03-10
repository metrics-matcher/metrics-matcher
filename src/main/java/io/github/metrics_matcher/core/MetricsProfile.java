package io.github.metrics_matcher.core;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Map;

@Value(staticConstructor = "of")
@EqualsAndHashCode(of = "name")
public class MetricsProfile {
    private String name;
    private Map<String, Object> params;
    private Map<String, Object> metrics;
}
