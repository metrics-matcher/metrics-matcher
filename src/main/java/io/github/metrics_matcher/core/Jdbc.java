package io.github.metrics_matcher.core;

import io.github.metrics_matcher.dto.DataSource;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;

@Slf4j
public class Jdbc implements AutoCloseable {

    public enum SpecialResult {
        NO_RESULT("No result"),
        MULTIPLE_ROWS("Multiple results");

        private String title;

        SpecialResult(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    private Connection connection;

    private void connect(DataSource ds) throws SQLException {
        if (connection == null || connection.isClosed()) {
            log.info("Opening JDBC connection [{}]", ds.getUrl());
            DriverManager.setLoginTimeout(ds.getTimeout());
            connection = DriverManager.getConnection(ds.getUrl(), ds.getUsername(), ds.getPassword());
            connection.setReadOnly(true);
        }
    }

    public Object execute(DataSource dataSource, String sql) throws SQLException {
        connect(dataSource);

        try (
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery();
        ) {
            log.info("Executing [{}]", sql);
            if (resultSet.next()) {
                Object value = resultSet.getObject(1);

                log.info("Received [{}]", value);
                if (resultSet.next()) {
                    log.warn("More than one row returned");
                    return SpecialResult.MULTIPLE_ROWS;
                } else {
                    return value;
                }
            } else {
                log.warn("Empty result");
                return SpecialResult.NO_RESULT;
            }
        }
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            log.info("Closing JDBC connection");
            connection.close();
        }
    }
}
