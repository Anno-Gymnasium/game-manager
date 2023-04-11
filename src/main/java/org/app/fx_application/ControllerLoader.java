package org.app.fx_application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

import org.app.game_classes.Account;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

public class ControllerLoader {
    private static final EventHandler<? super MouseEvent> rootOnMousePressed = (EventHandler<MouseEvent>) mouseEvent -> {
        Parent root = (Parent) mouseEvent.getSource();
        root.requestFocus();
    };
    public static Scene loadLoginController() throws IOException {
        FXMLLoader loginViewLoader = new FXMLLoader(Objects.requireNonNull(ControllerLoader.class.getClassLoader().getResource("login-view.fxml")));
        Parent loginRoot = loginViewLoader.load();
        loginRoot.setOnMousePressed(rootOnMousePressed);
        return new Scene(loginRoot);
    }
    public static Scene loadMainMenuController(@NotNull Account account) throws IOException {
        FXMLLoader mainMenuViewLoader = new FXMLLoader(Objects.requireNonNull(ControllerLoader.class.getClassLoader().getResource("main-menu-view.fxml")));
        Parent gameSelectionRoot = mainMenuViewLoader.load();
        gameSelectionRoot.setOnMousePressed(rootOnMousePressed);
        MainMenuController mainMenuController = mainMenuViewLoader.getController();
        mainMenuController.setAccount(account);
        return new Scene(gameSelectionRoot);
    }
}