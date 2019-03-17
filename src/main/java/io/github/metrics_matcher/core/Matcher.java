package io.github.metrics_matcher.core;

import io.github.metrics_matcher.dto.DataSource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
@Data
public class Matcher {
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final int DATE_LENGTH = "yyyy-MM-dd".length();

    private volatile boolean stopOnMismatch = false;
    private volatile boolean stopOnError = false;

    public final void run(DataSource dataSource, List<Task> tasks, Runnable progress)
            throws MetricsException {
        boolean hasError = false;
        boolean hasMismatch = false;
        for (Task task : tasks) {
            task.setResultValue("");
            task.setStatus(null);
        }

        try (Jdbc jdbc = new Jdbc()) {
            for (Task task : tasks) {
                if (hasError && stopOnError || hasMismatch && stopOnMismatch) {
                    task.setResultValue("");
                    task.setStatus(Task.Status.SKIP);
                    if (progress != null) {
                        progress.run();
                    }
                    continue;
                }

                long timestamp = System.currentTimeMillis();
                try {
                    Object result = jdbc.execute(dataSource, task.getQuerySql());

                    if (result == null) {
                        result = "null";
                    } else if (result instanceof Date) {
                        result = formatDate((Date) result, task.getExpectedValue());
                    }

                    task.setResultValue(result.toString());

                    if (result == Jdbc.SpecialResult.NO_RESULT ||
                            result == Jdbc.SpecialResult.MULTIPLE_ROWS) {
                        task.setStatus(Task.Status.ERROR);
                        task.setResultValue(result.toString());
                        hasError = true;
                    } else if (Objects.equals(result.toString(), task.getExpectedValue())) {
                        task.setStatus(Task.Status.OK);
                    } else {
                        task.setStatus(Task.Status.MISMATCH);
                        hasMismatch = true;
                    }
                } catch (SQLException e) {
                    task.setStatus(Task.Status.ERROR);
                    task.setResultValue(formatError(e));
                    hasError = true;
                } finally {
                    task.setDuration((System.currentTimeMillis() - timestamp) * 1000 / 1000000d);
                }

                if (progress != null) {
                    progress.run();
                }
            }
        } catch (SQLException e) {
            log.error("Can't close database connection", e);
            throw new MetricsException("Can't database connection", e);
        }
    }

    private static String formatDate(Date date, String expectedValue) {
        String resultDate = DATE_TIME_FORMAT.format(date);
        if (expectedValue != null && expectedValue.length() == DATE_LENGTH && resultDate.endsWith(" 00:00:00")) {
            return resultDate.substring(0, DATE_LENGTH);
        } else {
            return resultDate;
        }
    }

    private static String formatError(Exception e) {
        return e.getMessage().replaceAll("\\r\\n|\\r|\\n", " ");
    }
}
