package io.github.metrics_matcher;

import io.github.metrics_matcher.assets.DataSource;

import java.sql.*;

public class Xyz {
    public static void ping(DataSource dataSource, String sql) {
        DriverManager.setLoginTimeout(dataSource.getTimeout());
        try (Connection connection = DriverManager.getConnection(
                dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery();
        ) {

            if (resultSet.next()) {
                String tmp = resultSet.getString(1);
                System.out.println(tmp);
//            Map<String, String> row = new HashMap<>(columnCount);
//            for (int i = 1; i <= columnCount; i++) {
//                row.put(meta.getColumnName(i), resultSet.getString(i));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
