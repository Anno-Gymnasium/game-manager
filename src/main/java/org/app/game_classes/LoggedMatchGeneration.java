package org.app.game_classes;

import java.util.List;
import java.util.ArrayList;

public class LoggedMatchGeneration {
    private List<Match<MatchingTeam>> matches;

    public LoggedMatchGeneration() {
        matches = new ArrayList<>();
    }
    public LoggedMatchGeneration(List<Match<MatchingTeam>> matches) {
        this.matches = matches;
    }

    public void addMatch(Match<MatchingTeam> match) {
        matches.add(match);
    }
    public List<Match<MatchingTeam>> getMatches() {
        return matches;
    }
}