package io.github.metrics_matcher.dialogs;

import io.github.metrics_matcher.assets.AssetError;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

@Slf4j
public class AssetErrorDialog {

    private static final String HELP_URL = "https://metrics-matcher.github.io/";

    public static void show(String problem, AssetError e, String fixCode) {
        log.error(problem, e);

        ButtonType troubleshooting = new ButtonType("See how to troubleshoot this", ButtonBar.ButtonData.HELP);

        Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), troubleshooting);
        alert.setTitle("Error");
        alert.setHeaderText(problem);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == troubleshooting) {
            try {
                Desktop.getDesktop().browse(new URI(HELP_URL + fixCode));
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (URISyntaxException e1) {
                e1.printStackTrace();
            }
        }
    }
}
