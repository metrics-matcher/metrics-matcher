package io.github.metrics_matcher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.SneakyThrows;

import java.awt.*;
import java.io.IOException;
import java.util.logging.LogManager;

public class Main extends Application {
    private static final double APP_WIDTH = 960;
    private static final double APP_HEIGHT = 540;

    @SneakyThrows
    public static void main(String[] args) {
        LogManager.getLogManager().readConfiguration(
                Main.class.getClassLoader().getResourceAsStream("logger.properties")
        );

        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MetricsMatcher.fxml"));

        Parent root = loader.load();

        stage.setScene(new Scene(root, APP_WIDTH, APP_HEIGHT));
        stage.getIcons().addAll(new Image(Main.class.getResourceAsStream("images/icon.png")));

        final String version = getClass().getPackage().getImplementationVersion();

        stage.setTitle("Metrics matcher" + (version == null ? "" : " v" + version));
        stage.show();
        SplashScreen splashScreen = SplashScreen.getSplashScreen();
        if (splashScreen != null) {
            splashScreen.close();
        }
    }
}
