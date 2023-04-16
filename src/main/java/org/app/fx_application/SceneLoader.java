package org.app.fx_application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import org.app.GameMetadata;
import org.app.GameRole;
import org.app.fx_application.controllers.*;
import org.app.game_classes.Account;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

public class SceneLoader {
    private static final EventHandler<? super MouseEvent> rootOnMousePressed = (EventHandler<MouseEvent>) mouseEvent -> {
        Parent root = (Parent) mouseEvent.getSource();
        root.requestFocus();
    };

    public static void openLoginScene(Stage primaryStage) {
        Scene loginScene;
        try {
            loginScene = loadLoginScene();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        primaryStage.setTitle("Game Manager - Login");
        primaryStage.setScene(loginScene);
    }
    public static void openMainMenuScene(Stage primaryStage, Account currentAccount) {
        Scene mainMenuScene;
        MainMenuController controller;
        try {
            FXMLLoader mainMenuViewLoader = loadMainMenuScene();
            Parent mainMenuRoot = mainMenuViewLoader.load();
            mainMenuRoot.setOnMousePressed(rootOnMousePressed);
            controller = mainMenuViewLoader.getController();
            controller.setAccount(currentAccount);

            mainMenuScene = new Scene(mainMenuRoot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        primaryStage.setTitle("Game Manager - Hauptmenü (Angemeldet als: " + currentAccount.getName() + ")");
        primaryStage.setScene(mainMenuScene);

        // Eventhandler für das Reload-Event
        primaryStage.addEventHandler(MainMenuController.RELOAD, event -> controller.onReload());
    }
    public static void openGameScene(Stage primaryStage, GameMetadata metadata, GameRole joinRole) {
        Scene gameScene;
        try {
            gameScene = loadGameScene(metadata, joinRole);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        primaryStage.setTitle("Game Manager - Spiel (Angemeldet als: " + metadata.getAccount().getName() + ")");
        primaryStage.setScene(gameScene);
    }

    private static Scene loadLoginScene() throws IOException {
        FXMLLoader loginViewLoader = new FXMLLoader(Objects.requireNonNull(SceneLoader.class.getClassLoader().getResource("login-view.fxml")));
        Parent loginRoot = loginViewLoader.load();
        loginRoot.setOnMousePressed(rootOnMousePressed);
        return new Scene(loginRoot);
    }
    private static FXMLLoader loadMainMenuScene() throws IOException {
        return new FXMLLoader(Objects.requireNonNull(SceneLoader.class.getClassLoader().getResource("main-menu-view.fxml")));
    }
    private static Scene loadGameScene(@NotNull GameMetadata metadata, @NotNull GameRole joinRole) throws IOException {
        String fxmlPath = switch (metadata.getType()) {
            case MATCHLESS -> "matchless-game-view.fxml";
            case MATCHING -> "matching-game-view.fxml";
            case TREE -> "tree-game-view.fxml";
        };

        FXMLLoader gameViewLoader = new FXMLLoader(Objects.requireNonNull(SceneLoader.class.getClassLoader().getResource(fxmlPath)));
        Parent gameRoot = gameViewLoader.load();
        gameRoot.setOnMousePressed(rootOnMousePressed);

        switch (metadata.getType()) {
            case MATCHLESS -> {
                MatchlessGameController controller = gameViewLoader.getController();
                controller.loadGame(metadata, joinRole);
            }
            case MATCHING -> {
                MatchingGameController controller = gameViewLoader.getController();
                controller.loadGame(metadata, joinRole);
            }
            case TREE -> {
                TreeGameController controller = (TreeGameController) gameViewLoader.getController();
                controller.loadGame(metadata, joinRole);
            }
        }
        return new Scene(gameRoot);
    }
}