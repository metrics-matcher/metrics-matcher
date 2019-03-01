package io.github.metrics_matcher.dialogs;

import io.github.metrics_matcher.assets.AssetError;
import javafx.scene.control.Alert;

public class AssetErrorDialog {

    public static void show(AssetError e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(e.getMessage());
        alert.setContentText(e.getMessage());

        alert.showAndWait();
    }
}
