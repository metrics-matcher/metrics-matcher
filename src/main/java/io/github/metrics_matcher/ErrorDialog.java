package io.github.metrics_matcher;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import lombok.extern.java.Log;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

@Log
public class ErrorDialog {

    private static final String ERROR_TITLE = "Error";

    public static void show(String problem, Exception e) {
        log.severe(problem + "" + e.getMessage());

        Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
        alert.setTitle(ERROR_TITLE);
        alert.setHeaderText(problem);
        alert.showAndWait();
    }

    public static void show(String problem, Exception e, String helpUrl) {
        log.severe(problem + "" + e.getMessage());

        ButtonType troubleshooting = new ButtonType("See how to troubleshoot this", ButtonBar.ButtonData.HELP);

        Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), troubleshooting);
        alert.setTitle(ERROR_TITLE);
        alert.setHeaderText(problem);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == troubleshooting) {
            try {
                Desktop.getDesktop().browse(new URI(helpUrl));
            } catch (IOException | URISyntaxException ex) {
                log.severe("Can't open web browser. " + ex.getMessage());
            }
        }
    }
}
