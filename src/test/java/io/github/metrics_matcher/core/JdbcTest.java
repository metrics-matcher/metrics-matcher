package io.github.metrics_matcher.core;

import io.github.metrics_matcher.dto.DataSource;
import org.assertj.core.data.MapEntry;
import org.junit.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

public class JdbcTest {

    private DataSource dataSource = new DataSource("ds", "jdbc:h2:mem:", 100, "user", "pass", null);

    @Test
    public void checkResult() throws SQLException {
        try (Jdbc jdbc = new Jdbc()) {
            jdbc.connect(dataSource);
            assertThat(jdbc.execute("SELECT NULL").getData())
                    .containsOnly(MapEntry.entry("NULL", null));
            assertThat(jdbc.execute("SELECT 123").getData())
                    .containsOnly(MapEntry.entry("123", 123));
            assertThat(jdbc.execute("SELECT 'abc'").getData())
                    .containsOnly(MapEntry.entry("'abc'", "abc"));
            assertThat(jdbc.execute("SELECT 'abc' AS ABC").getData())
                    .containsOnly(MapEntry.entry("ABC", "abc"));
        }
    }

    @Test
    public void checkEmptyResult() throws SQLException {
        try (Jdbc jdbc = new Jdbc()) {
            jdbc.connect(dataSource);
            JdbcResult result = jdbc.execute("SELECT 1 WHERE 1=2");
            assertThat(result.hasError()).isTrue();
            assertThat(result.getError()).isSameAs(JdbcResult.Error.EMPTY_RESULT);
            assertThat(result.getData()).isNull();
        }
    }

    @Test
    public void checkMultipleResults() throws SQLException {
        try (Jdbc jdbc = new Jdbc()) {
            jdbc.connect(dataSource);
            JdbcResult result = jdbc.execute("SELECT 1 UNION SELECT 2");
            assertThat(result.hasError()).isTrue();
            assertThat(result.getError()).isSameAs(JdbcResult.Error.MULTIPLE_ROWS);
            assertThat(result.getData()).isNull();
        }
    }

    @Test
    public void checkAdditionalResult() throws SQLException {
        try (Jdbc jdbc = new Jdbc()) {
            jdbc.connect(dataSource);
            assertThat(jdbc.execute("SELECT 'a' AS actual, 'B' AS Add1, 3 AS add_2").getData())
                    .containsExactly(
                            MapEntry.entry("ACTUAL", "a"),
                            MapEntry.entry("ADD1", "B"),
                            MapEntry.entry("ADD_2", 3)
                    );
        }
    }
}