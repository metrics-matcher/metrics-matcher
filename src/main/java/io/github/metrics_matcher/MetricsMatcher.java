package io.github.metrics_matcher;

import io.github.metrics_matcher.xyz.DataSource;
import io.github.metrics_matcher.xyz.DataSources;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import lombok.extern.java.Log;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Log
public class MetricsMatcher implements Initializable {

    public Menu runMenu;
    public MenuItem runMenuItem;
    public MenuItem stopMenuItem;
    public Menu testsMenu;
    public MenuItem saveReportMenuItem;
    public Menu dataSourceMenu;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        loadDataSources();

        for (int i = 0; i < 10; i++) {
            CheckMenuItem testMenuItem = new CheckMenuItem("Test " + i);
            testMenuItem.setId("" + i);
            testsMenu.getItems().add(testMenuItem);
        }
    }

    private void loadDataSources() {
        try {
            List<DataSource> dataSources = DataSources.loadFromJsonFile("datasources.json");
            dataSources.forEach(ds -> {
                CheckMenuItem menuItem = new CheckMenuItem(ds.getName());
                dataSourceMenu.getItems().add(menuItem);
            });
            //todo hint button on empty
        } catch (IOException e) {
            //todo hint button
            e.printStackTrace();
        }
    }

    public void runAction(ActionEvent e) {
        System.out.println("run");
        runMenuItem.setDisable(true);
        stopMenuItem.setDisable(false);

        saveReportMenuItem.setDisable(true);
        testsMenu.getItems().forEach(menuItem -> menuItem.setDisable(true));
        dataSourceMenu.getItems().forEach(menuItem -> menuItem.setDisable(true));
    }

    public void stopAction(ActionEvent e) {
        runMenuItem.setDisable(false);
        stopMenuItem.setDisable(true);

        saveReportMenuItem.setDisable(false);
        testsMenu.getItems().forEach(menuItem -> menuItem.setDisable(false));
        dataSourceMenu.getItems().forEach(menuItem -> menuItem.setDisable(false));

    }
}
