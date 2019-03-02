package io.github.metrics_matcher.table;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ScopeRow {
    String metricsProfile;
    String query;
    Object expectedValue;
    Object actualValue;
    String executionStatus;
    Double executionTime;
}
