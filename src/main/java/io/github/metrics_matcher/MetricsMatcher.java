package io.github.metrics_matcher;

import io.github.metrics_matcher.core.AssetsLoader;
import io.github.metrics_matcher.core.Matcher;
import io.github.metrics_matcher.core.MetricsException;
import io.github.metrics_matcher.core.Task;
import io.github.metrics_matcher.dialogs.AboutDialog;
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
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

@Slf4j
@SuppressWarnings("checkstyle:VisibilityModifier")
public class MetricsMatcher implements Initializable {

    private static final PseudoClass EMPTY_PSEUDO_CLASS = PseudoClass.getPseudoClass("empty");
    private static final PseudoClass ROWNUM_PSEUDO_CLASS = PseudoClass.getPseudoClass("rownum");
    private static final PseudoClass OK_PSEUDO_CLASS = PseudoClass.getPseudoClass("ok");
    private static final PseudoClass MISMATCH_PSEUDO_CLASS = PseudoClass.getPseudoClass("mismatch");
    private static final PseudoClass ERROR_PSEUDO_CLASS = PseudoClass.getPseudoClass("error");
    private static final PseudoClass SKIP_PSEUDO_CLASS = PseudoClass.getPseudoClass("skip");

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

        rownumColumn.setCellFactory(column -> new TableCell<Task, String>() {
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : Integer.toString(getIndex() + 1));
                pseudoClassStateChanged(ROWNUM_PSEUDO_CLASS, true);
            }
        });

        executionStatus.setCellFactory(column -> new TableCell<Task, Task.Status>() {
            protected void updateItem(Task.Status item, boolean empty) {
                super.updateItem(item, empty);

                toggleRowClass(this, item);

                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.name());
                }
            }
        });
        table.setItems(tasks);
    }

    private static void toggleRowClass(TableCell tableCell, Task.Status status) {
        tableCell.pseudoClassStateChanged(MISMATCH_PSEUDO_CLASS, false);
        tableCell.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, false);
        tableCell.pseudoClassStateChanged(OK_PSEUDO_CLASS, false);
        tableCell.pseudoClassStateChanged(SKIP_PSEUDO_CLASS, false);

        if (status == null) {
            return;
        }

        switch (status) {
            case OK:
                tableCell.pseudoClassStateChanged(OK_PSEUDO_CLASS, true);
                break;
            case MISMATCH:
                tableCell.pseudoClassStateChanged(MISMATCH_PSEUDO_CLASS, true);
                break;
            case ERROR:
                tableCell.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, true);
                break;
            case SKIP:
                tableCell.pseudoClassStateChanged(SKIP_PSEUDO_CLASS, true);
                break;
            default:
                break;
        }
    }

    private void reloadDataSources() {
        dataSourceMenu.getItems().clear();
        runMenuItem.setDisable(true);
        selectedDataSourceLabel.setText("None");

        List<DataSource> dataSources;
        try {
            dataSources = AssetsLoader.loadDataSources("configs/data-sources.json");
        } catch (MetricsException e) {
            ErrorDialog.show("Can't read data sources", e, HelpRefs.DATA_SOURCES);
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
            ErrorDialog.show("Can't read metrics profiles", e, HelpRefs.METRICS_PROFILES);
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
            ErrorDialog.show("Can't read queries", e, HelpRefs.QUERIES);
        }
    }

    private void loadDrivers() {
        try {
            AssetsLoader.loadDrivers("drivers");
        } catch (MetricsException e) {
            ErrorDialog.show("Can't load drivers", e, HelpRefs.DRIVERS);
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
                    //sleep();
                });
            } catch (MetricsException e) {
                ErrorDialog.show("Can't run tasks", e);
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
        label.pseudoClassStateChanged(EMPTY_PSEUDO_CLASS, counter == 0);
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

    public void onlineHelpAction() {
        try {
            Desktop.getDesktop().browse(new URI(HelpRefs.HELP_URL));
        } catch (IOException | URISyntaxException ex) {
            log.error("Can't open web browser", ex);
        }
    }

    public void aboutAction() {
        AboutDialog.show();
    }
}
