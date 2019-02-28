package io.github.metrics_matcher;

import io.github.metrics_matcher.assets.*;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadDataSources();
        loadMetricsProfiles();
        loadQueries();
        loadDrivers();
    }

    private void loadDataSources() {
        try {
            List<DataSource> dataSources = AssetsLoader.loadDataSources("configs/data-sources.json");
            dataSources.forEach(ds -> {
                RadioMenuItem menuItem = new RadioMenuItem(ds.getName());
                dataSourceMenu.getItems().add(menuItem);
            });
            //todo hint button on empty
        } catch (AssetError e) {
            //todo hint button
            e.printStackTrace();
        }
    }

    private void loadMetricsProfiles() {
        try {
            List<MetricsProfile> metricsProfiles = AssetsLoader.loadMetricsProfiles("configs/metrics-profiles.json");
            metricsProfiles.forEach(mp -> {
                CheckMenuItem menuItem = new CheckMenuItem(mp.getName());
                metricsProfilesMenu.getItems().add(menuItem);
            });
            //todo hint button on empty
        } catch (AssetError e) {
            //todo hint button
            e.printStackTrace();
        }
    }

    private void loadQueries() {
        try {
            List<Query> metricsProfiles = AssetsLoader.loadQueries("queries");
        } catch (AssetError e) {
            e.printStackTrace();
        }
    }

    private void loadDrivers() {
        try {
            AssetsLoader.loadDrivers("drivers");
        } catch (AssetError e) {
            e.printStackTrace();
        }
    }

    public void runAction(ActionEvent e) {
        log.debug("Run");
        try (Jdbc jdbc = new Jdbc()) {

            jdbc.execute(DataSource.of("Test", "jdbc:h2:mem:test", 300, "xxx", "yyy"), "SELECT 1 FROM DUAL");
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
}
