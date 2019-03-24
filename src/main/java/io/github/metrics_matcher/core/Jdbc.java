package io.github.metrics_matcher.core;

import io.github.metrics_matcher.dto.DataSource;
import lombok.extern.java.Log;

import java.sql.*;

import static java.lang.String.format;

@Log
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
            log.info(format("Opening JDBC connection [%s]", ds.getUrl()));
            DriverManager.setLoginTimeout(ds.getTimeout());
            connection = DriverManager.getConnection(ds.getUrl(), ds.getUsername(), ds.getPassword());
            connection.setReadOnly(true);
        }
    }

    public Object execute(DataSource dataSource, String sql) throws SQLException {
        connect(dataSource);

        try (
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            log.info(format("Executing [%s]", sql));
            if (resultSet.next()) {
                Object value = resultSet.getObject(1);

                log.info(format("Received [%s]", value));
                if (resultSet.next()) {
                    log.warning("More than one row returned");
                    return SpecialResult.MULTIPLE_ROWS;
                } else {
                    return value;
                }
            } else {
                log.warning("Empty result");
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
