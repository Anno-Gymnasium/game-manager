package org.app.fx_application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import org.app.GameRole;
import org.app.GameType;
import org.app.game_classes.Account;
import org.app.game_classes.GenericGame;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

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
        try {
            mainMenuScene = loadMainMenuScene(currentAccount);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        primaryStage.setTitle("Game Manager - Hauptmenü (Angemeldet als: " + currentAccount.getName() + ")");
        primaryStage.setScene(mainMenuScene);
    }
    public static void openGameScene(GameType gameType, Stage primaryStage, UUID gameId, Account currentAccount, GameRole accountRole) {
        Scene gameScene;
        try {
            gameScene = loadGameScene(gameType, gameId, currentAccount, accountRole);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        primaryStage.setTitle("Game Manager - Spiel (Angemeldet als: " + currentAccount.getName() + ")");
        primaryStage.setScene(gameScene);
    }

    private static Scene loadLoginScene() throws IOException {
        FXMLLoader loginViewLoader = new FXMLLoader(Objects.requireNonNull(SceneLoader.class.getClassLoader().getResource("login-view.fxml")));
        Parent loginRoot = loginViewLoader.load();
        loginRoot.setOnMousePressed(rootOnMousePressed);
        return new Scene(loginRoot);
    }
    private static Scene loadMainMenuScene(@NotNull Account account) throws IOException {
        FXMLLoader mainMenuViewLoader = new FXMLLoader(Objects.requireNonNull(SceneLoader.class.getClassLoader().getResource("main-menu-view.fxml")));
        Parent gameSelectionRoot = mainMenuViewLoader.load();
        gameSelectionRoot.setOnMousePressed(rootOnMousePressed);
        MainMenuController mainMenuController = mainMenuViewLoader.getController();
        mainMenuController.setAccount(account);
        return new Scene(gameSelectionRoot);
    }
    private static <G extends GenericGame<?, ?>> Scene loadGameScene(@NotNull GameType gameType, @NotNull UUID gameId, @NotNull Account account, @NotNull GameRole accountRole) throws IOException {
        String fxmlPath;
        switch (gameType) {
            case MATCHLESS -> fxmlPath = "matchless-game-view.fxml";
            case MATCHING -> fxmlPath = "matching-game-view.fxml";
            case TREE -> fxmlPath = "tree-game-view.fxml";
            default -> fxmlPath = null;
        }

        FXMLLoader gameViewLoader = new FXMLLoader(Objects.requireNonNull(SceneLoader.class.getClassLoader().getResource(fxmlPath)));
        Parent gameRoot = gameViewLoader.load();
        gameRoot.setOnMousePressed(rootOnMousePressed);
        GameController<G> gameController = gameViewLoader.getController();
        gameController.setAccount(account);
        gameController.setAccountRole(accountRole);
        gameController.loadGame(gameId);
        return new Scene(gameRoot);
    }
}