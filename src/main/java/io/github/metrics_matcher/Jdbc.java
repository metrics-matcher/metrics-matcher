package io.github.metrics_matcher;

import io.github.metrics_matcher.assets.DataSource;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;

@Slf4j
public class Jdbc implements AutoCloseable {

    private Connection connection;

    private void connect(DataSource ds) throws SQLException {
        if (connection == null || connection.isClosed()) {
            log.info("Opening JDBC connection to [{}]", ds.getUrl());
            DriverManager.setLoginTimeout(ds.getTimeout());
            connection = DriverManager.getConnection(ds.getUrl(), ds.getUsername(), ds.getPassword());
            connection.setReadOnly(true);
        }
    }

    public Object execute(DataSource dataSource, String sql) {
        try {
            connect(dataSource);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery();
        ) {
            if (resultSet.next()) {
                String tmp = resultSet.getString(1);
                System.out.println(tmp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            log.info("Closing JDBC connection");
            connection.close();
        }
    }
}
