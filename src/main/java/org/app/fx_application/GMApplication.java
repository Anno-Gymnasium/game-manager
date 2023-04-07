package org.app.fx_application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.util.Objects;

public class GMApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        EventHandler<? super MouseEvent> rootOnMousePressed = (EventHandler<MouseEvent>) mouseEvent -> {
            Parent root = (Parent) mouseEvent.getSource();
            root.requestFocus();
        };
        FXMLLoader loginViewLoader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("login-view.fxml")));
        Parent loginRoot = loginViewLoader.load();
        loginRoot.setOnMousePressed(rootOnMousePressed);
        Scene loginScene = new Scene(loginRoot);
        LoginController loginController = loginViewLoader.getController();

        FXMLLoader gameSelectionViewLoader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("game-selection-view.fxml")));
        Parent gameSelectionRoot = gameSelectionViewLoader.load();
        gameSelectionRoot.setOnMousePressed(rootOnMousePressed);
        Scene gameSelectionScene = new Scene(gameSelectionRoot);
        GameSelectionController gameSelectionController = gameSelectionViewLoader.getController();

        loginController.setGameSelectionScene(gameSelectionScene);
        gameSelectionController.setLoginScene(loginScene);

        // Der andere Controller wird dem Login-Controller übergeben, damit dieser ihm den eingeloggten Account übergeben kann
        loginController.setGameSelectionController(gameSelectionController);

        primaryStage.setOnCloseRequest(windowEvent -> {
            Platform.exit();
            System.exit(0);
        });

        primaryStage.setResizable(false);
        primaryStage.setTitle("Game Manager Login");
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }
}