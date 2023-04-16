package org.app.fx_application.controllers;

import javafx.fxml.FXML;

import javafx.scene.control.*;
import org.app.GameMetadata;
import org.app.GameRole;
import org.app.fx_application.JdbiProvider;
import org.app.fx_application.selectables.GamePreview;
import org.app.game_classes.*;

import org.jdbi.v3.core.Jdbi;

public abstract class GameController<G extends GenericGame<?, ?>> {
    private Jdbi jdbi = JdbiProvider.getInstance().getJdbi();
    private GameMetadata metadata;
    private GameRole joinRole;
    private G game = null;

    @FXML protected Menu gameMenu;
    @FXML protected MenuItem miGameInfoSettings;
    @FXML protected Menu teamsMenu;
    @FXML protected SeparatorMenuItem sepTeamsMenu;
    @FXML protected MenuItem miCreateTeam, miMyTeam, miAllTeams;
    @FXML protected Menu playerMenu;
    @FXML protected SeparatorMenuItem sepPlayerMenu;
    @FXML protected MenuItem miMyPlayer, miAllPlayers;

    public void initialize() {

    }

    // Übernimmt die in preview übergebenen Zusatzdaten und lädt das Spiel aus der Datenbank
    public void loadGame(GameMetadata metadata, GameRole joinRole) {
        this.metadata = metadata;
        this.joinRole = joinRole;
        // Der Rest wird in den Subklassen durchgeführt (Spiel initialisieren und aus der Datenbank zusammensetzen)
    }

    @FXML
    private void onGameInfoSettings() {
        openIngameInfoDialog();
    }
    private void openIngameInfoDialog() {
//        IngameInfoDialog dialog = new IngameInfoDialog(metadata);
//        dialog.showAndWait().ifPresent(result -> {
//            game.updatePublicView();
//            game.updateAllowOwnTeamsCreation();
//        });
    }
}