package io.github.metrics_matcher.core;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Data;
import lombok.SneakyThrows;

import java.sql.SQLException;
import java.util.*;

@Data
public final class Engine {

    private DataSource dataSource;
    private final List<MetricsProfile> metricsProfiles = new ArrayList<>();

    private final ObservableList<Task> tasks = FXCollections.observableArrayList();

    @SneakyThrows
    private static void sleep() {
        Thread.sleep((long) (Math.random() * 2000));
    }

    public final void update(Collection<Query> queries) {
        tasks.clear();

        for (MetricsProfile metricsProfile : metricsProfiles) {
            for (Map.Entry<String, Object> metrics : metricsProfile.getMetrics().entrySet()) {
                for (Query query : queries) {
                    if (metrics.getKey().equals(query.getId())) {
                        String querySql = applyParams(query.getSql(), metricsProfile.getParams());
                        String queryTitle = Objects.toString(query.getTitle(), query.getId());

                        Task task = Task.builder()
                                .metricsProfile(metricsProfile.getName())
                                .queryTitle(queryTitle)
                                .querySql(querySql)
                                .expected(metrics.getValue())
                                .build();
                        tasks.add(task);
                    }
                }
            }
        }
    }

    private static String applyParams(String sql, Map<String, Object> params) {
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                sql = sql.replace("${" + entry.getKey() + "}", entry.getValue().toString());
            }
        }
        return sql;
    }

    public final void run(Runnable progress) throws MetricsException {
        try (Jdbc jdbc = new Jdbc()) {
            for (Task task : tasks) {
                long timestamp = System.currentTimeMillis();
                try {
                    Object result = jdbc.execute(dataSource, task.getQuerySql());
                    sleep();
                    task.setResult(result);

                    if (result != null && Objects.equals(result, task.getExpected())) {
                        task.setStatus(Task.Status.OK);
                    } else {
                        task.setStatus(Task.Status.MISMATCH);
                    }
                } catch (SQLException e) {
                    task.setStatus(Task.Status.ERROR);
                    task.setResult(e.getMessage());
                } finally {
                    task.setDuration((System.currentTimeMillis() - timestamp) * 1000 / 1000000d);
                }
                progress.run();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MetricsException("Can't create connection", e);
        }
    }
}
