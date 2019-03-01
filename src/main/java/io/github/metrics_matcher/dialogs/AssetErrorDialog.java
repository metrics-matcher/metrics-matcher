package io.github.metrics_matcher.dialogs;

import io.github.metrics_matcher.assets.AssetError;
import javafx.scene.control.Alert;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AssetErrorDialog {

    public static void show(String problem, AssetError e) {
        log.error(problem, e);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(problem);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }
}
