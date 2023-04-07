package org.app.game_classes;

public class DuplicateException extends Exception {
    public DuplicateException(String errorMessage) {
        super(errorMessage);
    }

    public DuplicateException() {
        super("Doppelter Teamname oder Spieler.");
    }
}
