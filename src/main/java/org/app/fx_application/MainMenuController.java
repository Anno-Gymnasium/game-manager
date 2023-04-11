package org.app.fx_application;

import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.app.game_classes.Account;

import java.util.UUID;

public class MainMenuController {
    @FXML private TextField gameSearchField;
    @FXML private CheckBox cbPublicViewFilter, cbAdminFilter, cbPlayerFilter, cbViewerFilter;
    @FXML private Button bCreateMatchlessGame, bCreateMatchingGame, bCreateTreeGame, bCreateWhitelistRequest;
    @FXML private Button bAccountSettings, bLogout;
    @FXML private VBox vbMatchlessGames, vbMatchingGames, vbTreeGames;
    @FXML private VBox vbOutgoingRequests, vbIncomingRequests, vbRequestAnswers, vbGameInvitations;

    private Account account;

    public void initialize() {
        account = null;
        vbMatchlessGames.getChildren().add(new SelectableGame(UUID.randomUUID(),
                "Test Game", "This is a test game", (byte) 0));
    }

    public void setAccount(Account account) {
        this.account = account;
        System.out.println(account);
    }
}