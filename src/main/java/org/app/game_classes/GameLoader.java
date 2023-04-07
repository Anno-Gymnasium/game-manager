package org.app.game_classes;

import java.util.Properties;

public class GameLoader {
    private Account currentAccount;

    public GameLoader() {

    }

    /** Lädt alle Spiele aus der Datenbank, zu denen der aktuelle Account Zugriffsrechte hat, in die übergebene GameCollection. */
    public void loadAccessibleGames(GameCollection gameCollection) {
        if (currentAccount == null) return;

        // TODO: Zu Ende implementieren
    }
    /** Aktualisiert die Spiele in der Datenbank, die in der übergebenen GameCollection enthalten sind. */
    public void updateGamesInDatabase(GameCollection gameCollection) {
        if (currentAccount == null) return;

        // TODO: Zu Ende implementieren
    }

    public void setAccount(Account account) {
        currentAccount = account;
    }
    public Account getAccount() {
        return currentAccount;
    }
}