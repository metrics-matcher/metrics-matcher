package io.github.metrics_matcher.core;

import io.github.metrics_matcher.dto.DataSource;
import lombok.Data;
import lombok.extern.java.Log;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Log
@Data
public class Matcher {
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final int DATE_LENGTH = "yyyy-MM-dd".length();

    private volatile boolean stop;
    private volatile boolean stopOnMismatch;
    private volatile boolean stopOnError;

    public final void run(DataSource dataSource, List<Task> tasks, Runnable progress)
            throws MetricsException {
        stop = false;
        boolean hasError = false;
        boolean hasMismatch = false;

        tasks.forEach(Task::reset);

        try (Jdbc jdbc = new Jdbc()) {
            for (Task task : tasks) {
                if (stop || hasError && stopOnError || hasMismatch && stopOnMismatch) {
                    task.setSkipState();
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

                    if (result == Jdbc.SpecialResult.NO_RESULT || result == Jdbc.SpecialResult.MULTIPLE_ROWS) {
                        task.setErrorState(result.toString());
                        hasError = true;
                    } else if (Objects.equals(result.toString(), task.getExpectedValue())) {
                        task.setOkState(result.toString());
                    } else {
                        task.setMismatchState(result.toString());
                        hasMismatch = true;
                    }
                } catch (SQLException e) {
                    task.setErrorState(formatError(e));
                    hasError = true;
                }

                task.setDuration(round3(System.currentTimeMillis() - timestamp));

                if (progress != null) {
                    progress.run();
                }
            }
        } catch (SQLException e) {
            log.severe("Can't close database connection." + e.getMessage());
            throw new MetricsException("Can't database connection", e);
        }
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    private static double round3(long value) {
        return value * 1000 / 1000000d;
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
