package io.github.metrics_matcher.core;

import io.github.metrics_matcher.dto.MetricsProfile;
import io.github.metrics_matcher.dto.Query;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskTest {

    private static final Map<String, String> METRICS = new HashMap<>();

    static {
        METRICS.put("select-1", "1");
    }


    @Test
    public void tasksFrom_empty() {
        List<Task> tasks = Task.tasksFrom(Collections.emptyList(), Collections.emptyList());
        assertThat(tasks).isEmpty();
    }

    @Test
    public void tasksFrom_noQueries() {
        List<Task> tasks = Task.tasksFrom(Collections.singletonList(
                new MetricsProfile("Metrics Set", METRICS, null)
        ), Collections.emptyList());
        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getMetricsProfileName()).isEqualTo("Metrics Set");
        assertThat(tasks.get(0).getMetricsId()).isEqualTo("select-1");
        assertThat(tasks.get(0).getQuerySql()).isNull();
        assertThat(tasks.get(0).getQueryTitle()).isEqualTo("!!! Query not found !!!");
        assertThat(tasks.get(0).getExpectedValue()).isEqualTo("1");
    }

    @Test
    public void tasksFrom_metricsAndQuery() {
        List<Task> tasks = Task.tasksFrom(Collections.singletonList(
                new MetricsProfile("Metrics Set", METRICS, null)
        ), Collections.singleton(
                new Query("select-1", "Select 1", "SELECT 1 FROM DUAL")
        ));
        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getMetricsProfileName()).isEqualTo("Metrics Set");
        assertThat(tasks.get(0).getMetricsId()).isEqualTo("select-1");
        assertThat(tasks.get(0).getQuerySql()).isEqualTo("SELECT 1 FROM DUAL");
        assertThat(tasks.get(0).getQueryTitle()).isEqualTo("Select 1");
        assertThat(tasks.get(0).getExpectedValue()).isEqualTo("1");
    }
    
    @Test
    public void tasksFrom_validParam() {
        List<Task> tasks = Task.tasksFrom(Collections.singletonList(
                new MetricsProfile("Metrics Set", METRICS, Collections.singletonMap("myParam", "123"))
        ), Collections.singleton(
                new Query("select-1", "Select 1 Where ${myParam}", "SELECT 1 FROM DUAL WHERE 1=${myParam}")
        ));
        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getMetricsProfileName()).isEqualTo("Metrics Set");
        assertThat(tasks.get(0).getMetricsId()).isEqualTo("select-1");
        assertThat(tasks.get(0).getQuerySql()).isEqualTo("SELECT 1 FROM DUAL WHERE 1=123");
        assertThat(tasks.get(0).getQueryTitle()).isEqualTo("Select 1 Where 123");
        assertThat(tasks.get(0).getExpectedValue()).isEqualTo("1");
    }

    @Test
    public void tasksFrom_paramNotFound() {
        List<Task> tasks = Task.tasksFrom(Collections.singletonList(
                new MetricsProfile("Metrics Set", METRICS, Collections.singletonMap("noParam", "yyy"))
        ), Collections.singleton(
                new Query("select-1", "Select 1 Where ${myParam}", "SELECT 1 FROM DUAL WHERE 1=${myParam}")
        ));
        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getMetricsProfileName()).isEqualTo("Metrics Set");
        assertThat(tasks.get(0).getMetricsId()).isEqualTo("select-1");
        assertThat(tasks.get(0).getQuerySql()).isEqualTo("SELECT 1 FROM DUAL WHERE 1=${myParam}");
        assertThat(tasks.get(0).getQueryTitle()).isEqualTo("Select 1 Where ${myParam}");
        assertThat(tasks.get(0).getExpectedValue()).isEqualTo("1");
    }
}