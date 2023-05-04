package org.app.fx_application;

import javafx.application.Application;
import javafx.application.Platform;

import javafx.stage.Stage;

public class GMApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setOnCloseRequest(windowEvent -> {
            Platform.exit();
            System.exit(0);
        });

        primaryStage.setResizable(false);

        SceneLoader.openLoginScene(primaryStage);
        primaryStage.show();
    }
}