package io.github.metrics_matcher.dto;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Map;

@Value
@EqualsAndHashCode(of = "name")
public class MetricsProfile {
    private String name;
    private Map<String, String> metrics;
    private Map<String, String> params;
}
