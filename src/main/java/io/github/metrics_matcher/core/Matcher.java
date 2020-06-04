package io.github.metrics_matcher.core;

import io.github.metrics_matcher.dto.DataSource;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Log
@Data
public class Matcher {
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private volatile boolean stop;
    private volatile boolean stopOnMismatch;
    private volatile boolean stopOnError;

    private boolean runHasError;
    private boolean runHasMismatch;

    public final void run(DataSource dataSource, List<Task> tasks, Consumer<Integer> progress)
            throws MetricsException {
        validateDatasource(dataSource);

        stop = false;
        runHasError = false;
        runHasMismatch = false;

        tasks.forEach(Task::reset);

        setFirstTaskRunningState(tasks, progress);

        try (Jdbc jdbc = new Jdbc()) {
            jdbc.connect(dataSource);

            for (int i = 0; i < tasks.size(); i++) {
                Task task = tasks.get(i);
                Task nextTask = i < tasks.size() - 1 ? tasks.get(i + 1) : null;

                runTask(jdbc, task);

                if (nextTask != null) {
                    nextTask.setRunningState();
                }

                progress.accept(i);
            }
        } catch (SQLException e) {
            log.severe("Connection problem: " + e.getMessage());
            throw new MetricsException("Connection problem", e);
        }
    }

    private void runTask(Jdbc jdbc, Task task) {
        long timestamp = System.currentTimeMillis();
        if (stop || runHasError && stopOnError || runHasMismatch && stopOnMismatch) {
            task.setSkipState();
        } else {
            try {
                JdbcResult result = jdbc.execute(task.getQuerySql());
                if (result.hasError()) {
                    task.setErrorState(result.getError().toString());
                    runHasError = true;
                } else {
                    String actualValue = formatResultValue(toActualResult(result.getData()));

                    if (Objects.equals(actualValue, task.getExpectedValue())) {
                        task.setOkState(actualValue);
                    } else {
                        task.setMismatchState(actualValue);
                        runHasMismatch = true;
                    }
                    task.setAdditionalResult(toAdditionalResult(result.getData()));
                }
            } catch (SQLException e) {
                task.setErrorState(formatError(e));
                runHasError = true;
            }
        }

        task.setDuration(round3(System.currentTimeMillis() - timestamp));
    }

    private static void validateDatasource(DataSource dataSource) throws MetricsException {
        if (dataSource.getUsername() == null || dataSource.getUsername().trim().isEmpty()) {
            throw new MetricsException("Database username must be set");
        }
        if (dataSource.getPassword() == null || dataSource.getPassword().trim().isEmpty()) {
            throw new MetricsException("Database password must be set");
        }
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    private static String round3(long value) {
        return "" + value * 1000 / 1000000d;
    }

    private static void setFirstTaskRunningState(List<Task> tasks, Consumer<Integer> progress) {
        if (!tasks.isEmpty()) {
            tasks.get(0).setRunningState();
            progress.accept(0);
        }
    }

    private static String toActualResult(Map<String, Object> data) {
        return formatResultValue(data.entrySet().iterator().next().getValue());
    }

    private static String toAdditionalResult(Map<String, Object> data) {
        return data.entrySet().stream().skip(1).
                map(entry -> entry.getKey() + "=" + formatResultValue(entry.getValue())).
                collect(Collectors.joining("; "));
    }

    private static String formatResultValue(Object value) {
        String result;
        if (value == null) {
            result = "null";
        } else if (value instanceof java.sql.Date) {
            result = DATE_FORMAT.format(value);
        } else if (value instanceof java.sql.Timestamp) {
            result = DATE_TIME_FORMAT.format(value);
        } else {
            result = value.toString();
        }
        return result;
    }

    private static String formatError(Exception e) {
        return e.getMessage().replaceAll("\\r\\n|\\r|\\n", " ");
    }

    @SneakyThrows
    @SuppressWarnings("checkstyle:MagicNumber")
    private static void sleep() {
        Thread.sleep((long) (Math.random() * 3000));
    }

}
