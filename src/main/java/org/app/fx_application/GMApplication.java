package org.app.fx_application;

import javafx.application.Application;
import javafx.application.Platform;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class GMApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setOnCloseRequest(windowEvent -> {
            Platform.exit();
            System.exit(0);
        });

        primaryStage.setResizable(false);
        primaryStage.setTitle("Game Manager Login");

        Scene loginScene = ControllerLoader.loadLoginController();
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }
}