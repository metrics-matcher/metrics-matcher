package io.github.metrics_matcher.core;

import io.github.metrics_matcher.dto.DataSource;
import org.junit.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

public class JdbcTest {

    @Test
    public void execute() throws SQLException {
        DataSource dataSource = new DataSource("ds", "jdbc:h2:mem:", 100, "user", "pass");
        try (Jdbc jdbc = new Jdbc()) {
            assertThat(jdbc.execute(dataSource, "SELECT NULL")).isNull();
            assertThat(jdbc.execute(dataSource, "SELECT 123")).isEqualTo(123);
            assertThat(jdbc.execute(dataSource, "SELECT 'abc'")).isEqualTo("abc");
            assertThat(jdbc.execute(dataSource, "SELECT 1 WHERE 1=2")).isEqualTo(Jdbc.SpecialResult.NO_RESULT);
        }
    }
}