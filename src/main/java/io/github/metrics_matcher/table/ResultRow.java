package io.github.metrics_matcher.table;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ResultRow {
    String metricsProfile;
    String query;
    String expectedValue;
    String actualValue;
    String executionStatus;
    Double executionTime;
}
