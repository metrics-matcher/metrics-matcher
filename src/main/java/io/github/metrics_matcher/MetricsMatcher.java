package io.github.metrics_matcher;

import io.github.metrics_matcher.assets.*;
import io.github.metrics_matcher.dialogs.AssetErrorDialog;
import io.github.metrics_matcher.dialogs.NotImplementedDialog;
import io.github.metrics_matcher.table.ScopeRow;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.sql.SQLException;
import java.util.*;

@Slf4j
public class MetricsMatcher implements Initializable {

    public Menu runMenu;
    public MenuItem runMenuItem;
    public MenuItem stopMenuItem;
    public Menu metricsProfilesMenu;
    public MenuItem saveReportMenuItem;
    public Menu dataSourceMenu;
    public MenuItem synchronizeMenuItem;
    public TableView<ScopeRow> table;
    public TableColumn<ScopeRow, String> rownumColumn;
    public Label selectedDataSourceLabel;
    public Tooltip selectedDataSourceTooltip;

    private ToggleGroup dataSourceToggleGroup = new ToggleGroup();

    public final StringProperty selectedDataSourceText = new SimpleStringProperty();
    public final StringProperty selectedDataSourceTooltipText = new SimpleStringProperty();

    private ObservableList<ScopeRow> scopeOfWork = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initBindings();
        synchronizeAction();
        createTable();
    }

    private void initBindings() {
        selectedDataSourceLabel.textProperty().bind(selectedDataSourceText);
        selectedDataSourceTooltip.textProperty().bind(selectedDataSourceTooltipText);
    }

    private void createTable() {
        rownumColumn.setCellFactory(column -> new TableCell<ScopeRow, String>() {
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

        scopeOfWork.addAll(
                ScopeRow.builder()
                        .metricsProfile("Dummy study fast check")
//                        .query("Connection check")
                        .expectedValue("1")
                        .actualValue("1")
                        .executionStatus("OK")
                        .executionTime(1.23)
                        .build(),
                ScopeRow.builder()
                        .metricsProfile("Dummy study fast check")
//                        .query("select-1-notitle")
                        .expectedValue("1")
                        .actualValue("1")
                        .executionStatus("FAIL")
                        .executionTime(1.23)
                        .build(),
                ScopeRow.builder()
                        .metricsProfile("Dummy study full check")
//                        .query("select-1-notitle")
                        .expectedValue("1")
                        .actualValue("Oralce error: #123")
                        .executionStatus("ERROR")
                        .executionTime(1.23)
                        .build()
        );

        PseudoClass failRowPseudoClass = PseudoClass.getPseudoClass("fail");
        PseudoClass errorRowPseudoClass = PseudoClass.getPseudoClass("error");

        table.setRowFactory(tv -> new TableRow<ScopeRow>() {
            @Override
            public void updateItem(ScopeRow item, boolean empty) {
                super.updateItem(item, empty);
                pseudoClassStateChanged(failRowPseudoClass, false);
                pseudoClassStateChanged(errorRowPseudoClass, false);

                if (!empty && item.getExecutionStatus() != null) {
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

        table.setItems(scopeOfWork);
    }

    private DataSource selectedDataSource;
    private Set<MetricsProfile> selectedMetricsProfiles = new LinkedHashSet<>();

    private void reloadDataSources() {
        dataSourceMenu.getItems().clear();
        try {
            List<DataSource> dataSources = AssetsLoader.loadDataSources("configs/data-sources.json");
            boolean sameAsSelected = false;
            for (DataSource dataSource : dataSources) {
                RadioMenuItem menuItem = new RadioMenuItem(dataSource.getName());
                menuItem.setToggleGroup(dataSourceToggleGroup);
                menuItem.setOnAction(e -> {
                    selectedDataSource = dataSource;
                    selectedDataSourceText.setValue(selectedDataSource.getName());
                    selectedDataSourceTooltipText.setValue(selectedDataSource.getUrl());
                });
                if (dataSource.equals(selectedDataSource)) {
                    menuItem.setSelected(true);
                    sameAsSelected = true;
                }
                dataSourceMenu.getItems().add(menuItem);
            }
            if (!sameAsSelected) {
                selectedDataSource = null;
                selectedDataSourceText.setValue("None");
                selectedDataSourceTooltipText.setValue(null);
            }
        } catch (AssetError e) {
            AssetErrorDialog.show("Can't read data sources", e, "#data-sources");
        }
    }

    private void reloadMetricsProfiles() {
        metricsProfilesMenu.getItems().clear();
        try {
            List<MetricsProfile> metricsProfiles = AssetsLoader.loadMetricsProfiles("configs/metrics-profiles.json");

            for (MetricsProfile metricsProfile : metricsProfiles) {
                CheckMenuItem menuItem = new CheckMenuItem(metricsProfile.getName());
                menuItem.setOnAction(e -> {
                    if (menuItem.isSelected()) {
                        selectedMetricsProfiles.add(metricsProfile);
                        recalculateScope();
                    }
                });
                metricsProfilesMenu.getItems().add(menuItem);
            }
        } catch (AssetError e) {
            AssetErrorDialog.show("Can't read metrics profiles", e, "#metrics-profiles");
        }
    }

    private void recalculateScope() {
        scopeOfWork.clear();

        for (MetricsProfile selectedMetricsProfile : selectedMetricsProfiles) {
            for (Map.Entry<String, Object> metrics : selectedMetricsProfile.getMetrics().entrySet()) {
                for (Query query : queries) {
                    if (metrics.getKey().equals(query.getId())) {
                        ScopeRow scopeRow = ScopeRow.builder()
                                .metricsProfile(selectedMetricsProfile.getName())
                                .query(query)
                                .expectedValue(metrics.getValue())
                                .build();
                        scopeOfWork.add(scopeRow);
                    }
                }

            }
        }
    }

    private List<Query> queries;

    private void loadQueries() {
        try {
            queries = AssetsLoader.loadQueries("queries");
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

    public void runAction() {
        //todo
        runLockMenuItems(true);
        try (Jdbc jdbc = new Jdbc()) {
            for (ScopeRow scopeRow : scopeOfWork) {
                Object result = jdbc.execute(selectedDataSource, scopeRow.getQuery().getSql());
                log.debug("" + result);
            }

        } catch (SQLException e1) {
            e1.printStackTrace();
        }
    }

    public final void stopAction() {
        runLockMenuItems(false);
    }

    private void runLockMenuItems(boolean lock) {
        runMenuItem.setDisable(lock);
        stopMenuItem.setDisable(!lock);
        metricsProfilesMenu.getItems().forEach(menuItem -> menuItem.setDisable(lock));
        dataSourceMenu.getItems().forEach(menuItem -> menuItem.setDisable(lock));
    }

    public final void showNotImplemented() {
        NotImplementedDialog.show();
    }

    public final void synchronizeAction() {
        reloadDataSources();
        reloadMetricsProfiles();
        loadQueries();
        loadDrivers();
    }

    public final void exitAction() {
        Platform.exit();
    }
}
