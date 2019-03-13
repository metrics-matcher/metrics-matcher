package io.github.metrics_matcher.core;


import io.github.metrics_matcher.dto.DataSource;
import io.github.metrics_matcher.dto.MetricsProfile;
import io.github.metrics_matcher.dto.Query;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Data;
import lombok.SneakyThrows;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

@Data
public final class Matcher {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private DataSource dataSource;
    private final List<MetricsProfile> metricsProfiles = new ArrayList<>();
    private volatile boolean stopOnMismatch = false;
    private volatile boolean stopOnError = false;

    private final ObservableList<Task> tasks = FXCollections.observableArrayList();

    @SneakyThrows
    private static void sleep() {
        Thread.sleep((long) (Math.random() * 1000));
    }

    public final void update(Collection<Query> queries) {
        tasks.clear();

        for (MetricsProfile metricsProfile : metricsProfiles) {
            for (Map.Entry<String, String> metrics : metricsProfile.getMetrics().entrySet()) {
                for (Query query : queries) {
                    if (metrics.getKey().equals(query.getId())) {
                        String querySql = applyParams(query.getSql(), metricsProfile.getParams());
                        String queryTitle = Objects.toString(query.getTitle(), query.getId());

                        Task task = Task.builder()
                                .metricsProfile(metricsProfile.getName())
                                .queryTitle(queryTitle)
                                .querySql(querySql)
                                .expected(Objects.toString(metrics.getValue(), "NULL"))
                                .build();
                        tasks.add(task);
                    }
                }
            }
        }
    }

    private static String applyParams(String sql, Map<String, String> params) {
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sql = sql.replace("${" + entry.getKey() + "}", entry.getValue());
            }
        }
        return sql;
    }


    public final void run(Runnable progress) throws MetricsException {
        boolean hasError = false;
        boolean hasMismatch = false;
        try (Jdbc jdbc = new Jdbc()) {
            for (Task task : tasks) {
                if (hasError && stopOnError || hasMismatch && stopOnMismatch) {
                    task.setResult("");
                    task.setStatus(Task.Status.SKIP);
                    continue;
                }
                long timestamp = System.currentTimeMillis();
                try {
                    Object result = jdbc.execute(dataSource, task.getQuerySql());

                    sleep();

                    if (result == null) {
                        result = "NULL";
                    } else if (result instanceof Date) {
                        if (task.getExpected() != null) {
                            if (task.getExpected().length() == DATE_FORMAT.toPattern().length()) {
                                result = DATE_FORMAT.format(result);
                            } else if (task.getExpected().length() == DATE_TIME_FORMAT.toPattern().length()) {
                                result = DATE_TIME_FORMAT.format(result);
                            }
                        }
                    }

                    task.setResult(result.toString());

                    if (Objects.equals(result.toString(), task.getExpected())) {
                        task.setStatus(Task.Status.OK);
                    } else {
                        task.setStatus(Task.Status.MISMATCH);
                        hasMismatch = true;
                    }
                } catch (SQLException e) {
                    task.setStatus(Task.Status.ERROR);
                    task.setResult(formatError(e));
                    hasError = true;
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

    private static String formatError(Exception e) {
        return e.getMessage().replaceAll("\\r\\n|\\r|\\n", " ");
    }
}
