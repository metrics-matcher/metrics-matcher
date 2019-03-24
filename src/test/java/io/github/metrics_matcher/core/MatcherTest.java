package io.github.metrics_matcher.core;

import io.github.metrics_matcher.dto.DataSource;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class MatcherTest {

    private static final DataSource DATA_SOURCE = new DataSource("ds", "jdbc:h2:mem:", 100, "user", "pass");

    private static Task newTask(String sql, String expectedValue) {
        return Task.builder().querySql(sql).expectedValue(expectedValue).build();
    }

    private static final Runnable PROGRESS = () -> {};

    @Test
    public void run_checkIntegerMatch() throws MetricsException {
        Task task = newTask("SELECT 123 FROM DUAL", "123");
        new Matcher().run(DATA_SOURCE, Collections.singletonList(task), PROGRESS);
        assertThat(task.getStatus()).isEqualTo(Task.Status.OK);
        assertThat(task.getResultValue()).isEqualTo("123");
    }

    @Test
    public void run_checkIntegerMismatch() throws MetricsException {
        Task task = newTask("SELECT 123 FROM DUAL", "234");
        new Matcher().run(DATA_SOURCE, Collections.singletonList(task), PROGRESS);
        assertThat(task.getStatus()).isEqualTo(Task.Status.MISMATCH);
        assertThat(task.getResultValue()).isEqualTo("123");
    }

    @Test
    public void run_checkFloatMatch() throws MetricsException {
        Task task = newTask("SELECT 1.23 FROM DUAL", "1.23");
        new Matcher().run(DATA_SOURCE, Collections.singletonList(task), PROGRESS);
        assertThat(task.getStatus()).isEqualTo(Task.Status.OK);
        assertThat(task.getResultValue()).isEqualTo("1.23");
    }

    @Test
    public void run_checkFloatMismatch() throws MetricsException {
        Task task = newTask("SELECT 1.23 FROM DUAL", "2.34");
        new Matcher().run(DATA_SOURCE, Collections.singletonList(task), PROGRESS);
        assertThat(task.getStatus()).isEqualTo(Task.Status.MISMATCH);
        assertThat(task.getResultValue()).isEqualTo("1.23");
    }

    @Test
    public void run_checkStringMatch() throws MetricsException {
        Task task = newTask("SELECT 'ABC' FROM DUAL", "ABC");
        new Matcher().run(DATA_SOURCE, Collections.singletonList(task), PROGRESS);
        assertThat(task.getStatus()).isEqualTo(Task.Status.OK);
        assertThat(task.getResultValue()).isEqualTo("ABC");
    }

    @Test
    public void run_checkStringMismatch() throws MetricsException {
        Task task = newTask("SELECT 'ABC' FROM DUAL", "XYZ");
        new Matcher().run(DATA_SOURCE, Collections.singletonList(task), PROGRESS);
        assertThat(task.getStatus()).isEqualTo(Task.Status.MISMATCH);
        assertThat(task.getResultValue()).isEqualTo("ABC");
    }

    @Test
    public void run_checkDateMatch() throws MetricsException {
        Task task = newTask("SELECT TO_DATE('2019-02-28', 'YYYY-MM-DD') FROM DUAL", "2019-02-28");
        new Matcher().run(DATA_SOURCE, Collections.singletonList(task), PROGRESS);
        assertThat(task.getStatus()).isEqualTo(Task.Status.OK);
        assertThat(task.getResultValue()).isEqualTo("2019-02-28");
    }

    @Test
    public void run_checkDateMismatch() throws MetricsException {
        Task task = newTask("SELECT TO_DATE('2019-02-28', 'YYYY-MM-DD') FROM DUAL", "2019-03-15");
        new Matcher().run(DATA_SOURCE, Collections.singletonList(task), PROGRESS);
        assertThat(task.getStatus()).isEqualTo(Task.Status.MISMATCH);
        assertThat(task.getResultValue()).isEqualTo("2019-02-28");
    }

    @Test
    public void run_checkDateTimeMatch() throws MetricsException {
        Task task = newTask("SELECT TO_DATE('2019-02-28 15:25:59', 'YYYY-MM-DD HH24:MI:SS') FROM DUAL",
                "2019-02-28 15:25:59");
        new Matcher().run(DATA_SOURCE, Collections.singletonList(task), PROGRESS);
        assertThat(task.getStatus()).isEqualTo(Task.Status.OK);
        assertThat(task.getResultValue()).isEqualTo("2019-02-28 15:25:59");
    }

    @Test
    public void run_checkDateTimeMismatch() throws MetricsException {
        Task task = newTask("SELECT TO_DATE('2019-02-28 15:25:59', 'YYYY-MM-DD HH24:MI:SS') FROM DUAL",
                "2019-02-28 13:13:13");
        new Matcher().run(DATA_SOURCE, Collections.singletonList(task), PROGRESS);
        assertThat(task.getStatus()).isEqualTo(Task.Status.MISMATCH);
        assertThat(task.getResultValue()).isEqualTo("2019-02-28 15:25:59");
    }


    @Test
    public void run_checkDateTimeDatePartMatch() throws MetricsException {
        Task task = newTask("SELECT TO_DATE('2019-02-28 00:00:00', 'YYYY-MM-DD HH24:MI:SS') FROM DUAL",
                "2019-02-28");
        new Matcher().run(DATA_SOURCE, Collections.singletonList(task), PROGRESS);
        assertThat(task.getStatus()).isEqualTo(Task.Status.OK);
        assertThat(task.getResultValue()).isEqualTo("2019-02-28");
    }

    @Test
    public void run_checkDateTimeDatePartMismatch() throws MetricsException {
        Task task = newTask("SELECT TO_DATE('2019-02-28 15:25:59', 'YYYY-MM-DD HH24:MI:SS') FROM DUAL",
                "2019-02-28");
        new Matcher().run(DATA_SOURCE, Collections.singletonList(task), PROGRESS);
        assertThat(task.getStatus()).isEqualTo(Task.Status.MISMATCH);
        assertThat(task.getResultValue()).isEqualTo("2019-02-28 15:25:59");
    }

    @Test
    public void run_nullMatch() throws MetricsException {
        Task task = newTask("SELECT NULL FROM DUAL", "null");
        new Matcher().run(DATA_SOURCE, Collections.singletonList(task), PROGRESS);
        assertThat(task.getStatus()).isEqualTo(Task.Status.OK);
        assertThat(task.getResultValue()).isEqualTo("null");
    }

    @Test
    public void run_nullMismatch() throws MetricsException {
        Task task = newTask("SELECT NULL FROM DUAL", "1");
        new Matcher().run(DATA_SOURCE, Collections.singletonList(task), PROGRESS);
        assertThat(task.getStatus()).isEqualTo(Task.Status.MISMATCH);
        assertThat(task.getResultValue()).isEqualTo("null");
    }

    @Test
    public void run_invalidSql() throws MetricsException {
        Task task = newTask("SELECT XXX FROM YYY", "1");
        new Matcher().run(DATA_SOURCE, Collections.singletonList(task), PROGRESS);
        assertThat(task.getStatus()).isEqualTo(Task.Status.ERROR);
        assertThat(task.getResultValue()).startsWith("Table \"YYY\" not found");
    }

    @Test
    public void run_noResult() throws MetricsException {
        Task task = newTask("SELECT 1 FROM DUAL WHERE 1=2", "1");
        new Matcher().run(DATA_SOURCE, Collections.singletonList(task), PROGRESS);
        assertThat(task.getStatus()).isEqualTo(Task.Status.ERROR);
        assertThat(task.getResultValue()).isEqualTo("No result");
    }

    @Test
    public void run_multipleResults() throws MetricsException {
        Task task = newTask("SELECT 1 FROM DUAL UNION SELECT 2 FROM DUAL", "1");
        new Matcher().run(DATA_SOURCE, Collections.singletonList(task), PROGRESS);
        assertThat(task.getStatus()).isEqualTo(Task.Status.ERROR);
        assertThat(task.getResultValue()).isEqualTo("Multiple results");
    }

    @Test
    public void run_emptyTasks() throws MetricsException {
        List<Task> tasks = Collections.emptyList();
        Matcher matcher = new Matcher();
        matcher.run(DATA_SOURCE, tasks, null);
    }

    @Test
    public void run_stopOnMismatch() throws MetricsException {
        List<Task> tasks = Arrays.asList(
                newTask("SELECT 1 FROM DUAL", "1"),
                newTask("SELECT 1 FROM DUAL", "2"),
                newTask("SELECT 1 FROM DUAL", "1")
        );
        Matcher matcher = new Matcher();
        matcher.setStopOnMismatch(true);
        matcher.run(DATA_SOURCE, tasks, null);

        assertThat(tasks.get(0).getStatus()).isEqualTo(Task.Status.OK);
        assertThat(tasks.get(1).getStatus()).isEqualTo(Task.Status.MISMATCH);
        assertThat(tasks.get(2).getStatus()).isEqualTo(Task.Status.SKIP);
    }

    @Test
    public void run_stopOnError() throws MetricsException {
        List<Task> tasks = Arrays.asList(
                newTask("SELECT 1 FROM DUAL", "1"),
                newTask("SELECT XXX FROM YYY", "1"),
                newTask("SELECT 1 FROM DUAL", "1")
        );
        Matcher matcher = new Matcher();
        matcher.setStopOnError(true);
        matcher.run(DATA_SOURCE, tasks, null);

        assertThat(tasks.get(0).getStatus()).isEqualTo(Task.Status.OK);
        assertThat(tasks.get(1).getStatus()).isEqualTo(Task.Status.ERROR);
        assertThat(tasks.get(2).getStatus()).isEqualTo(Task.Status.SKIP);
    }

    @Test
    public void run_invalidJdbc() throws MetricsException {
        Task task = newTask("SELECT 1 FROM DUAL", "1");
        new Matcher().run(new DataSource("ds", "jdbc:invalid:", 100, "user", "pass"),
                Collections.singletonList(task), PROGRESS);

        assertThat(task.getStatus()).isEqualTo(Task.Status.ERROR);
        assertThat(task.getResultValue()).startsWith("No suitable driver found");
    }
}