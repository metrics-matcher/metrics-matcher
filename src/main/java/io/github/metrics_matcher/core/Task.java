package io.github.metrics_matcher.core;

import io.github.metrics_matcher.dto.MetricsProfile;
import io.github.metrics_matcher.dto.Query;
import lombok.Builder;
import lombok.Data;

import java.util.*;

@Data
@Builder
public class Task {
    private final String metricsProfileName;
    private final String metricsId;
    private final String queryTitle;
    private final String querySql;

    private final String expectedValue;

    private String resultValue;
    private Status status;
    private Double duration;

    public enum Status {
        OK, MISMATCH, ERROR, SKIP
    }

    public void reset() {
        resultValue = null;
        status = null;
        duration = null;
    }

    public static List<Task> tasksFrom(List<MetricsProfile> metricsProfiles, Collection<Query> queries) {
        List<Task> tasks = new ArrayList<>();

        for (MetricsProfile metricsProfile : metricsProfiles) {
            for (Map.Entry<String, String> metrics : metricsProfile.getMetrics().entrySet()) {
                TaskBuilder taskBuilder = Task.builder().
                        metricsProfileName(metricsProfile.getName()).
                        metricsId(metrics.getKey()).
                        expectedValue(Objects.toString(metrics.getValue(), "null"));

                for (Query query : queries) {
                    if (metrics.getKey().equals(query.getId())) {
                        if (query.getSql() != null) {
                            taskBuilder.querySql(substituteParams(query.getSql(), metricsProfile.getParams()));
                        }
                        if (query.getTitle() != null) {
                            taskBuilder.queryTitle(substituteParams(query.getTitle(), metricsProfile.getParams()));
                        }
                        break;
                    }
                }

                if (taskBuilder.querySql == null) {
                    taskBuilder.queryTitle("!!! Query not found !!!");
                }
                tasks.add(taskBuilder.build());
            }
        }
        return tasks;
    }

    private static String substituteParams(String sql, Map<String, String> params) {
        String result = sql;
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                result = result.replace("${" + entry.getKey() + "}", entry.getValue());
            }
        }
        return result;
    }
}
