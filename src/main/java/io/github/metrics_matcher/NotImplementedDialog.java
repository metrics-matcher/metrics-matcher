package io.github.metrics_matcher;

import javafx.scene.control.Alert;

public class NotImplementedDialog {

    public static void show() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Not implemented");
        alert.setHeaderText("This feature is not yet implemented");
//        alert.setContentText(e.getMessage());

        alert.showAndWait();
    }
}
