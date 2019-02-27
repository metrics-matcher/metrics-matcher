package io.github.metrics_matcher;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import lombok.extern.java.Log;

import java.net.URL;
import java.util.ResourceBundle;

@Log
public class MetricsMatcher implements Initializable {

    public Menu runMenu;
    public MenuItem runMenuItem;
    public MenuItem stopMenuItem;
    public Menu testsMenu;
    public MenuItem saveReportMenuItem;
    public Menu datasourceMenu;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        for (int i = 0; i < 10; i++) {
            CheckMenuItem testMenuItem = new CheckMenuItem("Test " + i);
            testMenuItem.setId("" + i);
            testsMenu.getItems().add(testMenuItem);
        }
    }

    public void runAction(ActionEvent e) {
        System.out.println("run");
        runMenuItem.setDisable(true);
        stopMenuItem.setDisable(false);

        saveReportMenuItem.setDisable(true);
        testsMenu.getItems().forEach(menuItem -> menuItem.setDisable(true));
        datasourceMenu.getItems().forEach(menuItem -> menuItem.setDisable(true));
    }

    public void stopAction(ActionEvent e) {
        runMenuItem.setDisable(false);
        stopMenuItem.setDisable(true);

        saveReportMenuItem.setDisable(false);
        testsMenu.getItems().forEach(menuItem -> menuItem.setDisable(false));
        datasourceMenu.getItems().forEach(menuItem -> menuItem.setDisable(false));

    }
}
