package io.github.metrics_matcher.core;

import io.github.metrics_matcher.dto.DataSource;
import lombok.extern.java.Log;

import java.sql.*;
import java.util.LinkedHashMap;

import static java.lang.String.format;

@Log
public class Jdbc implements AutoCloseable {

    private Connection connection;

    public void connect(DataSource ds) throws SQLException {
        if (isOpenConnection()) {
            log.info("Closing existing JDBC connection");
            connection.close();
        }

        log.info(format("Opening JDBC connection [%s]", ds.getUrl()));
        DriverManager.setLoginTimeout(ds.getTimeout());
        connection = DriverManager.getConnection(ds.getUrl(), ds.getUsername(), ds.getPassword());
        connection.setReadOnly(true);
        if (ds.getSchema() != null) {
            connection.setSchema(ds.getSchema());
        }
    }

    public JdbcResult execute(String sql) throws SQLException {
        try (
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery();
        ) {
            log.info(format("Executing [%s]", sql));
            if (resultSet.next()) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnsCount = metaData.getColumnCount();

                LinkedHashMap<String, Object> data = new LinkedHashMap<>();

                for (int i = 1; i <= columnsCount; i++) {
                    data.put(metaData.getColumnLabel(i), resultSet.getObject(i));
                }

                log.info(format("Received [%s]", data));
                if (resultSet.next()) {
                    log.warning("More than one row returned");
                    return new JdbcResult(JdbcResult.Error.MULTIPLE_ROWS);
                } else {
                    return new JdbcResult(data);
                }
            } else {
                log.warning("Empty result");
                return new JdbcResult(JdbcResult.Error.EMPTY_RESULT);
            }
        }
    }

    private boolean isOpenConnection() throws SQLException {
        return connection != null && !connection.isClosed();
    }

    @Override
    public void close() throws SQLException {
        if (isOpenConnection()) {
            log.info("Closing JDBC connection");
            connection.close();
        }
    }
}
