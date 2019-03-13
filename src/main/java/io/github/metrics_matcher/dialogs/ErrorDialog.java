package io.github.metrics_matcher.dialogs;

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
public class ErrorDialog {


    public static void showError(String problem, Exception e) {
        log.error(problem, e);

        Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
        alert.setTitle("Error");
        alert.setHeaderText(problem);
    }

    public static void showError(String problem, Exception e, String helpUrl) {
        log.error(problem, e);

        ButtonType troubleshooting = new ButtonType("See how to troubleshoot this", ButtonBar.ButtonData.HELP);

        Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), troubleshooting);
        alert.setTitle("Error");
        alert.setHeaderText(problem);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == troubleshooting) {
            try {
                Desktop.getDesktop().browse(new URI(helpUrl));
            } catch (IOException | URISyntaxException e1) {
                log.error("Can't open web browser", e1);
            }
        }
    }
}
