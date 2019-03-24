package io.github.metrics_matcher.core;

import io.github.metrics_matcher.dto.DataSource;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Log
@Data
public class Matcher {
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final int DATE_LENGTH = "yyyy-MM-dd".length();

    private volatile boolean stop;
    private volatile boolean stopOnMismatch;
    private volatile boolean stopOnError;

    public final void run(DataSource dataSource, List<Task> tasks, Consumer<Integer> progress)
            throws MetricsException {
        stop = false;
        boolean hasError = false;
        boolean hasMismatch = false;

        tasks.forEach(Task::reset);

        if (!tasks.isEmpty()) {
            tasks.get(0).setRunningState();
            progress.accept(0);
        }

        try (Jdbc jdbc = new Jdbc()) {
            for (int i = 0; i < tasks.size(); i++) {
                Task task = tasks.get(i);
                Task nextTask = i < tasks.size() - 1 ? tasks.get(i + 1) : null;

                long timestamp = System.currentTimeMillis();
                if (stop || hasError && stopOnError || hasMismatch && stopOnMismatch) {
                    task.setSkipState();
                } else {
                    try {
                        Object result = jdbc.execute(dataSource, task.getQuerySql());

//                        sleep();

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
                }

                task.setDuration(round3(System.currentTimeMillis() - timestamp));

                if (nextTask != null) {
                    nextTask.setRunningState();
                }

                progress.accept(i);
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

    @SneakyThrows
    @SuppressWarnings("checkstyle:MagicNumber")
    private static void sleep() {
        Thread.sleep((long) (Math.random() * 3000));
    }

}
