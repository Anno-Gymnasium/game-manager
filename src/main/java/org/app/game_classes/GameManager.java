package org.app.game_classes;

import java.io.IOException;
import java.sql.SQLException;

public class GameManager {
    private GameCollection allGamesCollection;

    private Account currentAccount;

    private GameLoader gameLoader;

    public GameManager() throws SQLException, IOException {
        allGamesCollection = new GameCollection();
        currentAccount = null;
        gameLoader = new GameLoader();
    }

    public void loadAccessibleGames() {
        gameLoader.loadAccessibleGames(allGamesCollection);
    }
    public GameCollection searchGames(String query, boolean caseSensitive) {
        return allGamesCollection.filter(query, caseSensitive);
    }

    public void setAccount(Account account) {
        currentAccount = account;
    }
    public Account getAccount() {
        return currentAccount;
    }
}