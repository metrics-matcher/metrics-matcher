package io.github.metrics_matcher;

import io.github.metrics_matcher.assets.*;
import io.github.metrics_matcher.dialogs.AssetErrorDialog;
import io.github.metrics_matcher.dialogs.NotImplementedDialog;
import io.github.metrics_matcher.table.ResultRow;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
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
    public TableColumn<ResultRow, String> rownumColumn;

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
                        .metricsProfile("Dummy study fast check")
                        .query("Connection check")
                        .expectedValue("1")
                        .actualValue("1")
                        .executionStatus("OK")
                        .executionTime(1.23)
                        .build(),
                ResultRow.builder()
                        .metricsProfile("Dummy study fast check")
                        .query("select-1-notitle")
                        .expectedValue("1")
                        .actualValue("1")
                        .executionStatus("FAIL")
                        .executionTime(1.23)
                        .build(),
                ResultRow.builder()
                        .metricsProfile("Dummy study full check")
                        .query("select-1-notitle")
                        .expectedValue("1")
                        .actualValue("Oralce error: #123")
                        .executionStatus("ERROR")
                        .executionTime(1.23)
                        .build()
        );

        PseudoClass failRowPseudoClass = PseudoClass.getPseudoClass("fail");
        PseudoClass errorRowPseudoClass = PseudoClass.getPseudoClass("error");

        table.setRowFactory(tv -> new TableRow<ResultRow>() {
            @Override
            public void updateItem(ResultRow item, boolean empty) {
                super.updateItem(item, empty);
                pseudoClassStateChanged(failRowPseudoClass, false);
                pseudoClassStateChanged(errorRowPseudoClass, false);

                if (!empty) {
                    switch (item.getExecutionStatus()) {
                        case "FAIL":
                            pseudoClassStateChanged(failRowPseudoClass, true);
                            break;
                        case "ERROR":
                            pseudoClassStateChanged(errorRowPseudoClass, true);
                            break;
                    }
                }
            }
        });

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
