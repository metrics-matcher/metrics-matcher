package io.github.metrics_matcher;

import io.github.metrics_matcher.assets.*;
import io.github.metrics_matcher.dialogs.AssetErrorDialog;
import io.github.metrics_matcher.dialogs.NotImplementedDialog;
import io.github.metrics_matcher.table.ResultRow;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

@Slf4j
public class MetricsMatcher implements Initializable {

    public Menu runMenu;
    public MenuItem runMenuItem;
    public MenuItem stopMenuItem;
    public Menu metricsProfilesMenu;
    public MenuItem saveReportMenuItem;
    public Menu dataSourceMenu;
    public MenuItem reloadMenuItem;
    public TableView<ResultRow> table;
    public TableColumn rownumColumn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        synchronizeAction();
        createTable();
    }


    private void createTable() {
        rownumColumn.setCellFactory(column -> new TableCell<ResultRow, String>() {
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(Integer.toString(getIndex() + 1));
                }
                getStyleClass().add("-mm-rownum");
            }
        });

        ObservableList<ResultRow> results = FXCollections.observableArrayList(
                ResultRow.builder()
                        .metricsProfile("mp")
                        .query("Query")
                        .expectedValue("123")
                        .actualValue("234")
                        .executionStatus("OK")
                        .executionTime(1.23)
                        .build()
        );

        table.setItems(results);
    }

    private void reloadDataSources() {
        try {
            List<DataSource> dataSources = AssetsLoader.loadDataSources("configs/data-sources.json");
            dataSources.forEach(ds -> {
                RadioMenuItem menuItem = new RadioMenuItem(ds.getName());
                dataSourceMenu.getItems().add(menuItem);
            });
        } catch (AssetError e) {
            AssetErrorDialog.show("Can't read datasources", e, "#data-sources");
        }
    }

    private void reloadMetricsProfiles() {
        metricsProfilesMenu.getItems().clear();
        try {
            List<MetricsProfile> metricsProfiles = AssetsLoader.loadMetricsProfiles("configs/metrics-profiles.json");
            metricsProfiles.forEach(mp -> {
                CheckMenuItem menuItem = new CheckMenuItem(mp.getName());
                metricsProfilesMenu.getItems().add(menuItem);
            });
        } catch (AssetError e) {
            AssetErrorDialog.show("Can't read metrics profiles", e, "#metrics-profiles");
        }
    }

    private void loadQueries() {
        try {
            List<Query> queries = AssetsLoader.loadQueries("queries");
        } catch (AssetError e) {
            AssetErrorDialog.show("Can't read queries", e, "#queries");
        }
    }

    private void loadDrivers() {
        try {
            AssetsLoader.loadDrivers("drivers");
        } catch (AssetError e) {
            AssetErrorDialog.show("Can't load drivers", e, "#drivers");
        }
    }

    public void runAction(ActionEvent e) {
        log.debug("Run");
        try (Jdbc jdbc = new Jdbc()) {
            Object result = jdbc.execute(DataSource.of("Test", "jdbc:h2:mem:test", 300, "xxx", "yyy"), "SELECT 1 FROM DUAL");
        } catch (SQLException e1) {
            e1.printStackTrace();
        }

        runMenuItem.setDisable(true);
        stopMenuItem.setDisable(false);

        saveReportMenuItem.setDisable(true);
        metricsProfilesMenu.getItems().forEach(menuItem -> menuItem.setDisable(true));
        dataSourceMenu.getItems().forEach(menuItem -> menuItem.setDisable(true));
    }

    public void stopAction(ActionEvent e) {
        runMenuItem.setDisable(false);
        stopMenuItem.setDisable(true);

        saveReportMenuItem.setDisable(false);
        metricsProfilesMenu.getItems().forEach(menuItem -> menuItem.setDisable(false));
        dataSourceMenu.getItems().forEach(menuItem -> menuItem.setDisable(false));

    }

    public void showNotImplemented() {
        NotImplementedDialog.show();
    }

    public void synchronizeAction() {
        reloadDataSources();
        reloadMetricsProfiles();
        loadQueries();
        loadDrivers();
    }

    public void exitAction() {
        Platform.exit();
    }
}
