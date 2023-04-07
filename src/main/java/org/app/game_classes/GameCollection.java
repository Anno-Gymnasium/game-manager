package org.app.game_classes;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import java.util.UUID;

public class GameCollection {
    private HashMap<UUID, MatchlessGame> matchlessGames;
    private HashMap<UUID, MatchingGame> matchingGames;
    private HashMap<UUID, TreeGame> treeGames;

    public GameCollection() {
        matchlessGames = new HashMap<>();
        matchingGames = new HashMap<>();
        treeGames = new HashMap<>();
    }

    public void add(MatchlessGame game) {
        matchlessGames.put(game.getId(), game);
    }
    public void add(MatchingGame game) {
        matchingGames.put(game.getId(), game);
    }
    public void add(TreeGame game) {
        treeGames.put(game.getId(), game);
    }

    public List<MatchlessGame> getMatchlessGames() {
        return new ArrayList<>(matchlessGames.values());
    }
    public List<MatchingGame> getMatchingGames() {
        return new ArrayList<>(matchingGames.values());
    }
    public List<TreeGame> getTreeGames() {
        return new ArrayList<>(treeGames.values());
    }

    public GameCollection filter(String query, boolean caseSensitive) {
        if (!caseSensitive) query = query.toLowerCase();
        GameCollection result = new GameCollection();

        for (MatchlessGame game : matchlessGames.values()) {
            String name = caseSensitive ? game.getName() : game.getName().toLowerCase();
            if (name.contains(query)) {
                result.add(game);
            }
        }
        for (MatchingGame game : matchingGames.values()) {
            String name = caseSensitive ? game.getName() : game.getName().toLowerCase();
            if (name.contains(query)) {
                result.add(game);
            }
        }
        for (TreeGame game : treeGames.values()) {
            String name = caseSensitive ? game.getName() : game.getName().toLowerCase();
            if (name.contains(query)) {
                result.add(game);
            }
        }
        return result;
    }
}