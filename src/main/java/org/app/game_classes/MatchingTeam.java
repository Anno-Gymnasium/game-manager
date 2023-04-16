package org.app.game_classes;

import java.util.TreeMap;

/**  */
public class MatchingTeam extends GenericMatchingTeam<MatchingTeam> {
    public MatchingTeam(GlobalTeam globalTeam, int leagueIndex) {
        super(globalTeam, leagueIndex);
    }

    @Override
    public MatchingTeam cloneForNewLeague() {
        MatchingTeam clone = new MatchingTeam(globalTeam, leagueIndex);
        clone.playerByName = new TreeMap<>(playerByName);
        return clone;
    }
}