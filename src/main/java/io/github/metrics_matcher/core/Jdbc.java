package io.github.metrics_matcher.core;

import lombok.extern.slf4j.Slf4j;

import java.sql.*;

@Slf4j
public class Jdbc implements AutoCloseable {

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
                Object tmp = resultSet.getObject(1);
                log.info("Received [{}]", tmp);
                return tmp;
            } else {
                log.warn("Empty result");
                return null;
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
