package io.github.metrics_matcher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.util.logging.LogManager;

public class App extends Application {
    private static final double APP_WIDTH = 960;
    private static final double APP_HEIGHT = 540;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        LogManager.getLogManager().readConfiguration(getClass().getResourceAsStream("logger.properties"));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("MetricsMatcher.fxml"));

        MetricsMatcher controller = loader.getController();
        Parent root = loader.load();

        stage.setScene(new Scene(root, APP_WIDTH, APP_HEIGHT));

        stage.getIcons().addAll(new Image(getClass().getResourceAsStream("icon.png")));

        final String version = getClass().getPackage().getImplementationVersion();

        stage.setTitle("Metrics Matcher" + (version == null ? "" : " v" + version));
        stage.show();

        SplashScreen splashScreen = SplashScreen.getSplashScreen();
        if (splashScreen != null) {
            splashScreen.close();
        }
    }
}
