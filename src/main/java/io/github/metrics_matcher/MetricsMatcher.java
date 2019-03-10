package io.github.metrics_matcher;

import io.github.metrics_matcher.core.DataSource;
import io.github.metrics_matcher.core.MetricsException;
import io.github.metrics_matcher.core.MetricsProfile;
import io.github.metrics_matcher.core.Query;
import io.github.metrics_matcher.core.AssetsLoader;
import io.github.metrics_matcher.dialogs.ErrorDialog;
import io.github.metrics_matcher.dialogs.NotImplementedDialog;
import io.github.metrics_matcher.core.Engine;
import io.github.metrics_matcher.core.Task;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class MetricsMatcher implements Initializable {

    public Menu dataSourceMenu;
    public Menu metricsProfilesMenu;
    public Menu runMenu;

    public MenuItem runMenuItem;
    public MenuItem stopMenuItem;
    public MenuItem synchronizeMenuItem;

    public TableView<Task> table;
    public TableColumn<Task, String> rownumColumn;
    public TableColumn<Task, Task.Status> executionStatus;

    public Label selectedDataSourceLabel;
    public Tooltip selectedDataSourceTooltip;
    public Label selectedMetricsProfilesLabel;
    public Tooltip selectedMetricsProfilesTooltip;

    private final ToggleGroup dataSourceToggleGroup = new ToggleGroup();

    private final StringProperty selectedDataSourceText = new SimpleStringProperty();
    private final StringProperty selectedDataSourceTooltipText = new SimpleStringProperty();
    private final StringProperty selectedMetricsProfilesText = new SimpleStringProperty();
    private final StringProperty selectedMetricsProfilesTooltipText = new SimpleStringProperty();

    public ProgressBar progressBar;

    private List<Query> queries;

    private final Engine engine = new Engine();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initBindings();
        synchronizeAction();
        createTable();
        engine.getTasks().addAll(TempData.DATA);
    }

    private void initBindings() {
        selectedDataSourceLabel.textProperty().bind(selectedDataSourceText);
        selectedDataSourceTooltip.textProperty().bind(selectedDataSourceTooltipText);
        selectedMetricsProfilesLabel.textProperty().bind(selectedMetricsProfilesText);
        selectedMetricsProfilesTooltip.textProperty().bind(selectedMetricsProfilesTooltipText);
    }

    private void createTable() {
        PseudoClass rownumPseudoClass = PseudoClass.getPseudoClass("rownum");
        PseudoClass failRowPseudoClass = PseudoClass.getPseudoClass("fail");
        PseudoClass errorRowPseudoClass = PseudoClass.getPseudoClass("error");

        rownumColumn.setCellFactory(column -> new TableCell<Task, String>() {
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : Integer.toString(getIndex() + 1));
                pseudoClassStateChanged(rownumPseudoClass, true);
            }
        });

        executionStatus.setCellFactory(column -> new TableCell<Task, Task.Status>() {
            protected void updateItem(Task.Status item, boolean empty) {
                super.updateItem(item, empty);
                pseudoClassStateChanged(failRowPseudoClass, false);
                pseudoClassStateChanged(errorRowPseudoClass, false);

                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                setText(item.name());

                switch (item) {
                    case MISMATCH:
                        pseudoClassStateChanged(failRowPseudoClass, true);
                        break;
                    case ERROR:
                        pseudoClassStateChanged(errorRowPseudoClass, true);
                        break;
                }
            }
        });
        table.setItems(engine.getTasks());
    }

    private void reloadDataSources() {
        dataSourceMenu.getItems().clear();
        runMenuItem.setDisable(true);

        List<DataSource> dataSources;
        try {
            dataSources = AssetsLoader.loadDataSources("configs/data-sources.json");
        } catch (MetricsException e) {
            ErrorDialog.show("Can't read data sources", e, "#data-sources");
            return;
        }

        DataSource previousSelection = engine.getDataSource();
        engine.setDataSource(null);
        for (DataSource dataSource : dataSources) {
            RadioMenuItem menuItem = new RadioMenuItem(dataSource.getName());
            menuItem.setToggleGroup(dataSourceToggleGroup);
            menuItem.setOnAction(e -> {
                engine.setDataSource(dataSource);
                selectedDataSourceText.setValue(dataSource.getName());
                selectedDataSourceTooltipText.setValue(dataSource.getUrl());
                touchRunState();
            });
            if (dataSource.equals(previousSelection)) {
                engine.setDataSource(dataSource);
                menuItem.setSelected(true);
            }
            dataSourceMenu.getItems().add(menuItem);
        }

        touchRunState();

        if (engine.getDataSource() == null) {
            if (previousSelection == null && !dataSourceMenu.getItems().isEmpty()) {
                RadioMenuItem firstMenuItem = (RadioMenuItem) dataSourceMenu.getItems().get(0);
                firstMenuItem.setSelected(true);
                firstMenuItem.getOnAction().handle(null);
            } else {
                selectedDataSourceText.setValue("None");
                selectedDataSourceTooltipText.setValue(null);
            }
        }
    }

    private void reloadMetricsProfiles() {
        metricsProfilesMenu.getItems().clear();
        runMenuItem.setDisable(true);

        List<MetricsProfile> metricsProfiles;
        try {
            metricsProfiles = AssetsLoader.loadMetricsProfiles("configs/metrics-profiles.json");
        } catch (MetricsException e) {
            ErrorDialog.show("Can't read metrics profiles", e, "#metrics-profiles");
            return;
        }

        List<MetricsProfile> previousSelection = new ArrayList<>(engine.getMetricsProfiles());
        engine.getMetricsProfiles().clear();
        for (MetricsProfile metricsProfile : metricsProfiles) {
            CheckMenuItem menuItem = new CheckMenuItem(metricsProfile.getName());
            menuItem.setOnAction(e -> {
                if (menuItem.isSelected()) {
                    engine.getMetricsProfiles().add(metricsProfile);
                } else {
                    engine.getMetricsProfiles().remove(metricsProfile);
                }
                selectedMetricsProfilesText.setValue("" + engine.getMetricsProfiles().size());
                selectedMetricsProfilesTooltipText.setValue(
                        engine.getMetricsProfiles().stream().map(MetricsProfile::getName).collect(Collectors.joining(", "))
                );

                touchRunState();
                engine.update(queries);
            });
            if (previousSelection.contains(metricsProfile)) {
                engine.getMetricsProfiles().add(metricsProfile);
                menuItem.setSelected(true);
            }
            metricsProfilesMenu.getItems().add(menuItem);
        }

        touchRunState();

        if (engine.getMetricsProfiles().isEmpty()) {
            if (previousSelection.isEmpty() && !metricsProfilesMenu.getItems().isEmpty()) {
                CheckMenuItem firstMenuItem = (CheckMenuItem) metricsProfilesMenu.getItems().get(0);
                firstMenuItem.setSelected(true);
                firstMenuItem.getOnAction().handle(null);
            } else {
                selectedMetricsProfilesText.setValue("0");
                selectedMetricsProfilesTooltipText.setValue(null);
            }
        }
    }

    private void loadQueries() {
        try {
            queries = AssetsLoader.loadQueries("queries");
        } catch (MetricsException e) {
            ErrorDialog.show("Can't read queries", e, "#queries");
        }
    }

    private void loadDrivers() {
        try {
            AssetsLoader.loadDrivers("drivers");
        } catch (MetricsException e) {
            ErrorDialog.show("Can't load drivers", e, "#drivers");
        }
    }

    public void runAction() {
        runLockMenuItems(true);

        progressBar.setProgress(0);

        final double step = 1d / engine.getTasks().size();

        new Thread(() -> {
            try {
                engine.run(() -> Platform.runLater(() -> {
                    table.refresh();
                    progressBar.setProgress(progressBar.getProgress() + step);
                }));
            } catch (MetricsException e) {
                ErrorDialog.show("Can't run tasks", e);
            }
            runLockMenuItems(false);
        }).start();
    }

    public void stopAction() {
        //todo stop process
        runLockMenuItems(false);
    }

    private void touchRunState() {
        runMenuItem.setDisable(engine.getDataSource() == null || engine.getMetricsProfiles().isEmpty());
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
        loadQueries();
        reloadDataSources();
        reloadMetricsProfiles();
        loadDrivers();
    }

    public final void exitAction() {
        Platform.exit();
    }
}
