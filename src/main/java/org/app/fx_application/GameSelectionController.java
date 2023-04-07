package org.app.fx_application;

import javafx.fxml.FXML;
import javafx.scene.Scene;

import javafx.scene.control.Label;
import org.app.game_classes.Account;

public class GameSelectionController {
    @FXML private Label testLabel;

    private Account account;
    private Scene loginScene;

    public void initialize() {
        account = null;
        loginScene = null;
    }

    public void setLoginScene(Scene loginScene) {
        this.loginScene = loginScene;
    }
    public void setAccount(Account account) {
        this.account = account;
        System.out.println(account);
    }
}