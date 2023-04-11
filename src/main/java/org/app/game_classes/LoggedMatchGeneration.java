package org.app.game_classes;

import java.util.List;
import java.util.ArrayList;

public class LoggedMatchGeneration <T extends GenericMatchingTeam<T>> {
    private List<Match<T>> matches;

    public LoggedMatchGeneration() {
        matches = new ArrayList<>();
    }
    public LoggedMatchGeneration(List<Match<T>> matches) {
        this.matches = matches;
    }

    public void addMatch(Match<T> match) {
        matches.add(match);
    }
    public List<Match<T>> getMatches() {
        return matches;
    }
}