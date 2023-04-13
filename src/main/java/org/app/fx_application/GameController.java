package org.app.fx_application;

import javafx.fxml.FXML;

import javafx.scene.control.*;
import javafx.stage.Stage;
import org.app.GameRole;
import org.app.game_classes.*;

import java.util.UUID;

import org.jdbi.v3.core.Jdbi;

public abstract class GameController<G extends GenericGame<?, ?>> {
    private Jdbi jdbi = JdbiProvider.getInstance().getJdbi();
    private Account currentAccount = null;
    private GameRole accountRole = null;
    private G game = null;

    public void initialize() {

    }

    abstract void loadGame(UUID id);

    public void setAccount(Account account) {
        currentAccount = account;
    }
    public void setAccountRole(GameRole role) {
        accountRole = role;
    }
}