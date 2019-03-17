package io.github.metrics_matcher;

import io.github.metrics_matcher.core.AssetsLoader;
import io.github.metrics_matcher.core.Matcher;
import io.github.metrics_matcher.core.MetricsException;
import io.github.metrics_matcher.core.Task;
import io.github.metrics_matcher.dialogs.ErrorDialog;
import io.github.metrics_matcher.dialogs.NotImplementedDialog;
import io.github.metrics_matcher.dto.DataSource;
import io.github.metrics_matcher.dto.MetricsProfile;
import io.github.metrics_matcher.dto.Query;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Slf4j
@SuppressWarnings("checkstyle:VisibilityModifier")
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
    public Label selectedMetricsProfilesLabel;
    public Label selectedMetricsLabel;

    public ProgressBar progressBar;
    public HBox resultCounters;
    public Label counterOkLabel;
    public Label counterErrorLabel;
    public Label counterMismatchLabel;
    public Label counterSkipLabel;

    private DataSource selectedDataSource;
    private List<Query> queries;
    private final List<MetricsProfile> selectedMetricsProfiles = new ArrayList<>();
    private final ObservableList<Task> tasks = FXCollections.observableArrayList();
    private final ToggleGroup dataSourceToggleGroup = new ToggleGroup();
    private final Matcher matcher = new Matcher();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        synchronizeAction();
        createTable();
    }

    private void createTable() {
        PseudoClass rownumPseudoClass = PseudoClass.getPseudoClass("rownum");
        PseudoClass mismatchRowPseudoClass = PseudoClass.getPseudoClass("mismatch");
        PseudoClass errorRowPseudoClass = PseudoClass.getPseudoClass("error");
        PseudoClass skipRowPseudoClass = PseudoClass.getPseudoClass("skip");
        PseudoClass okRowPseudoClass = PseudoClass.getPseudoClass("ok");

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
                pseudoClassStateChanged(mismatchRowPseudoClass, false);
                pseudoClassStateChanged(errorRowPseudoClass, false);
                pseudoClassStateChanged(okRowPseudoClass, false);
                pseudoClassStateChanged(skipRowPseudoClass, false);

                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                setText(item.name());

                switch (item) {
                    case OK:
                        pseudoClassStateChanged(okRowPseudoClass, true);
                        break;
                    case MISMATCH:
                        pseudoClassStateChanged(mismatchRowPseudoClass, true);
                        break;
                    case ERROR:
                        pseudoClassStateChanged(errorRowPseudoClass, true);
                        break;
                    case SKIP:
                        pseudoClassStateChanged(skipRowPseudoClass, true);
                        break;
                    default:
                        break;
                }
            }
        });
        table.setItems(tasks);
    }

    private void reloadDataSources() {
        dataSourceMenu.getItems().clear();
        runMenuItem.setDisable(true);
        selectedDataSourceLabel.setText("None");

        List<DataSource> dataSources;
        try {
            dataSources = AssetsLoader.loadDataSources("configs/data-sources.json");
        } catch (MetricsException e) {
            ErrorDialog.showError("Can't read data sources", e, HelpRefs.DATA_SOURCES);
            return;
        }

        DataSource previousSelection = selectedDataSource;
        selectedDataSource = null;
        for (DataSource dataSource : dataSources) {
            RadioMenuItem menuItem = new RadioMenuItem(dataSource.getName());
            menuItem.setToggleGroup(dataSourceToggleGroup);
            menuItem.setOnAction(e -> {
                selectedDataSource = dataSource;
                selectedDataSourceLabel.setText(dataSource.getName());
                touchRunState();
            });
            if (dataSource.equals(previousSelection)) {
                selectedDataSource = dataSource;
                selectedDataSourceLabel.setText(dataSource.getName());
                menuItem.setSelected(true);
            }
            dataSourceMenu.getItems().add(menuItem);
        }

        touchRunState();

        if (selectedDataSource == null) {
            if (previousSelection == null && !dataSourceMenu.getItems().isEmpty()) {
                RadioMenuItem firstMenuItem = (RadioMenuItem) dataSourceMenu.getItems().get(0);
                firstMenuItem.setSelected(true);
                firstMenuItem.getOnAction().handle(null);
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
            ErrorDialog.showError("Can't read metrics profiles", e, HelpRefs.METRICS_PROFILES);
            return;
        }

        List<MetricsProfile> previousSelection = new ArrayList<>(selectedMetricsProfiles);
        selectedMetricsProfiles.clear();
        for (MetricsProfile metricsProfile : metricsProfiles) {
            CheckMenuItem menuItem = new CheckMenuItem(metricsProfile.getName());
            menuItem.setOnAction(e -> {
                if (menuItem.isSelected()) {
                    selectedMetricsProfiles.add(metricsProfile);
                } else {
                    selectedMetricsProfiles.remove(metricsProfile);
                }
                selectedMetricsProfilesLabel.setText("" + selectedMetricsProfiles.size());

                selectedMetricsLabel.setText(
                        "" + selectedMetricsProfiles.stream().mapToInt(mp -> mp.getMetrics().size()).sum()
                );

                touchRunState();
                tasks.setAll(Task.tasksFrom(selectedMetricsProfiles, queries));
            });
            if (previousSelection.contains(metricsProfile)) {
                selectedMetricsProfiles.add(metricsProfile);
                menuItem.setSelected(true);
            }
            metricsProfilesMenu.getItems().add(menuItem);
        }

        touchRunState();

        if (selectedMetricsProfiles.isEmpty()) {
            if (previousSelection.isEmpty() && !metricsProfilesMenu.getItems().isEmpty()) {
                CheckMenuItem firstMenuItem = (CheckMenuItem) metricsProfilesMenu.getItems().get(0);
                firstMenuItem.setSelected(true);
                firstMenuItem.getOnAction().handle(null);
            } else {
                selectedMetricsProfilesLabel.setText("0");
            }
        }
    }

    private void loadQueries() {
        try {
            queries = AssetsLoader.loadQueries("queries");
        } catch (MetricsException e) {
            queries = Collections.emptyList();
            ErrorDialog.showError("Can't read queries", e, HelpRefs.QUERIES);
        }
    }

    private void loadDrivers() {
        try {
            AssetsLoader.loadDrivers("drivers");
        } catch (MetricsException e) {
            ErrorDialog.showError("Can't load drivers", e, HelpRefs.DRIVERS);
        }
    }

    @SneakyThrows
    @SuppressWarnings("checkstyle:MagicNumber")
    private static void sleep() {
        Thread.sleep((long) (Math.random() * 1000));
    }

    public void runAction() {
        runLockMenuItems(true);

        resultCounters.setVisible(false);
        progressBar.setVisible(true);
        progressBar.setProgress(0);

        final double step = 1d / tasks.size();

        new Thread(() -> {
            try {
                matcher.run(selectedDataSource, tasks, () -> {
                    Platform.runLater(() -> {
                        table.refresh();
                        progressBar.setProgress(progressBar.getProgress() + step);
                    });
                    sleep();
                });
            } catch (MetricsException e) {
                ErrorDialog.showError("Can't run tasks", e);
            }
            Platform.runLater(() -> {
                progressBar.setVisible(false);
                updateCounters();
                resultCounters.setVisible(true);
                runLockMenuItems(false);
            });
        }).start();
    }


    private void updateCounters() {
        int countOk = 0;
        int countError = 0;
        int countMismatch = 0;
        int countSkip = 0;
        for (Task task : tasks) {
            switch (task.getStatus()) {
                case OK:
                    countOk++;
                    break;
                case ERROR:
                    countError++;
                    break;
                case MISMATCH:
                    countMismatch++;
                    break;
                case SKIP:
                    countSkip++;
                    break;
                default:
                    break;
            }
        }

        setCounterLabel(counterOkLabel, countOk);
        setCounterLabel(counterErrorLabel, countError);
        setCounterLabel(counterMismatchLabel, countMismatch);
        setCounterLabel(counterSkipLabel, countSkip);
    }

    private static void setCounterLabel(Label label, int counter) {
        label.setText("" + counter);
        label.getStyleClass().remove("empty");
        if (counter == 0) {
            label.getStyleClass().add("empty");
        }
    }

    public void stopAction() {
        matcher.setStop(true);
        runLockMenuItems(false);
    }

    private void touchRunState() {
        runMenuItem.setDisable(selectedDataSource == null || selectedMetricsProfiles.isEmpty());
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

    public void stopOnErrorAction(ActionEvent event) {
        if (event.getSource() instanceof CheckMenuItem) {
            CheckMenuItem checkMenuItem = (CheckMenuItem) event.getSource();
            matcher.setStopOnError(checkMenuItem.isSelected());
        }
    }

    public void stopOnMismatchAction(ActionEvent event) {
        if (event.getSource() instanceof CheckMenuItem) {
            CheckMenuItem checkMenuItem = (CheckMenuItem) event.getSource();
            matcher.setStopOnMismatch(checkMenuItem.isSelected());
        }
    }
}
