package org.app.fx_application.controllers;

import org.app.GameMetadata;
import org.app.GameRole;
import org.app.game_classes.TreeGame;

public class TreeGameController extends GameController<TreeGame> {
    public void initialize() {
        super.initialize();
    }

    @Override
    public void loadGame(GameMetadata metadata, GameRole joinRole) {
        super.loadGame(metadata, joinRole);
        // TODO: Spiel initialisieren und aus der Datenbank zusammensetzen
    }
}