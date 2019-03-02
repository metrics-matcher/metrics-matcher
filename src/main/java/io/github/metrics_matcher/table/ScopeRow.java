package io.github.metrics_matcher.table;

import io.github.metrics_matcher.assets.Query;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ScopeRow {
    String metricsProfile;
    Query query;
    Object expectedValue;
    Object actualValue;
    String executionStatus;
    Double executionTime;
}
