package io.github.metrics_matcher.dialogs;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AboutDialog {

    @SuppressWarnings("checkstyle:MagicNumber")
    public static void show() {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("About");
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setHeaderText("Metrics matcher is a free database testing tool.\n"
                + "Copyright (c) 2019 Metrics matcher, Xantorohara\n"
                + "MIT License"
        );
        dialog.setGraphic(new ImageView(new Image(AboutDialog.class.getResourceAsStream("../images/icon.png"))));

        dialog.getDialogPane().setPrefSize(400d, 220d);

        dialog.getDialogPane().setBackground(
                new Background(
                        new BackgroundImage(
                                new Image(AboutDialog.class.getResourceAsStream("../images/mushrooms.png")),
                                BackgroundRepeat.NO_REPEAT,
                                BackgroundRepeat.NO_REPEAT,
                                BackgroundPosition.CENTER,
                                BackgroundSize.DEFAULT)
                )
        );
        dialog.showAndWait();
    }
}
