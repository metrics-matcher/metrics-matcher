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
    private final String queryId;
    private final String queryTitle;
    private final String querySql;

    private final String expectedValue;

    private String resultValue;
    private Status status;
    private String duration;
    private String additionalResult;

    public enum Status {
        OK, MISMATCH, ERROR, SKIP, RUNNING, NONE
    }

    public void reset() {
        status = Status.NONE;
        resultValue = null;
        duration = null;
        additionalResult = null;
    }

    public void setRunningState() {
        this.status = Status.RUNNING;
    }

    public void setOkState(String resultValue) {
        this.status = Status.OK;
        this.resultValue = resultValue;
    }

    public void setMismatchState(String resultValue) {
        this.status = Status.MISMATCH;
        this.resultValue = resultValue;
    }

    public void setSkipState() {
        this.status = Status.SKIP;
    }

    public void setErrorState(String error) {
        this.status = Task.Status.ERROR;
        this.resultValue = error;
    }

    public static List<Task> tasksFrom(List<MetricsProfile> metricsProfiles, Collection<Query> queries) {
        List<Task> tasks = new ArrayList<>();

        for (MetricsProfile metricsProfile : metricsProfiles) {
            for (Map.Entry<String, String> metrics : metricsProfile.getMetrics().entrySet()) {
                TaskBuilder taskBuilder = Task.builder().
                        status(Status.NONE).
                        metricsProfileName(metricsProfile.getName()).
                        queryId(metrics.getKey()).
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
