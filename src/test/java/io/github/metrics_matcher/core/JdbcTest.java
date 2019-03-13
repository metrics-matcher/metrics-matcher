package io.github.metrics_matcher.core;

import io.github.metrics_matcher.dto.DataSource;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class JdbcTest {

    @Test
    void execute() throws SQLException {
        DataSource dataSource = new DataSource("ds", "jdbc:h2:mem:", 100, "user", "pass");
        try (Jdbc jdbc = new Jdbc()) {
            assertNull(jdbc.execute(dataSource, "SELECT NULL"));
            assertEquals(123, jdbc.execute(dataSource, "SELECT 123"));
            assertEquals("abc", jdbc.execute(dataSource, "SELECT 'abc'"));
            assertEquals(Jdbc.SpecialResult.EMPTY, jdbc.execute(dataSource, "SELECT 1 WHERE 1=2"));
        }
    }
}