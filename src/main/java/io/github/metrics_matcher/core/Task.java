package io.github.metrics_matcher.core;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Task {
    private final String metricsProfile;
    private final String queryTitle;
    private final String querySql;

    private final String expected;

    private String result;
    private Status status;
    private Double duration;

    public enum Status {
        OK, MISMATCH, ERROR, SKIP
    }
}
