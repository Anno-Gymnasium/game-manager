package org.app.fx_application.controllers;

import org.app.GameMetadata;
import org.app.GameRole;
import org.app.game_classes.MatchingGame;

public class MatchingGameController extends GameController<MatchingGame> {
    public void initialize() {
        super.initialize();
    }

    @Override
    public void loadGame(GameMetadata metadata, GameRole joinRole) {
        super.loadGame(metadata, joinRole);
        // TODO: Spiel initialisieren und aus der Datenbank zusammensetzen
    }
}