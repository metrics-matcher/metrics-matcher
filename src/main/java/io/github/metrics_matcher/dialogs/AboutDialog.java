package io.github.metrics_matcher.dialogs;

import javafx.scene.control.Alert;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AboutDialog {

    public static void show() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Lorem ipsum \n ZZZ");
        alert.setTitle("About");
        alert.showAndWait();
    }
}
