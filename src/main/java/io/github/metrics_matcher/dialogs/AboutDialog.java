package io.github.metrics_matcher.dialogs;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.StageStyle;

public class AboutDialog {

    @SuppressWarnings("checkstyle:MagicNumber")
    public static void show() {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Metrics matcher: magic mushrooms");
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setHeaderText("Metrics matcher is a data matching tool.\n"
                + "Copyright (c) 2019 Metrics matcher, Xantorohara\n"
                + "MIT License"
        );
        dialog.setGraphic(new ImageView(new Image(AboutDialog.class.getResourceAsStream("../images/icon.png"))));


        BackgroundImage backgroundImage = new BackgroundImage(
                new Image(AboutDialog.class.getResourceAsStream("../images/mushrooms.png")),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                BackgroundSize.DEFAULT);

        Label mushrooms = new Label();
        mushrooms.setPrefHeight(128);
        mushrooms.setBackground(new Background(backgroundImage));
        dialog.getDialogPane().setContent(mushrooms);
        dialog.showAndWait();
    }
}
