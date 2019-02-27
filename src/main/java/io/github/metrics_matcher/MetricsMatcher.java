package io.github.metrics_matcher;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void runAction(ActionEvent e) {
        System.out.println("run");
        runMenuItem.setDisable(true);
        stopMenuItem.setDisable(false);
    }

    public void stopAction(ActionEvent e) {
        runMenuItem.setDisable(false);
        stopMenuItem.setDisable(true);
    }
}
