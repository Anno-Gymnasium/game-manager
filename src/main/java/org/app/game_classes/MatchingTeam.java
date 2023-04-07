package org.app.game_classes;

import java.util.TreeMap;

/**  */
public class MatchingTeam extends GenericMatchingTeam<MatchingTeam> {
    public MatchingTeam(GlobalTeam globalTeam, int leagueIndex, Player player) {
        super(globalTeam, leagueIndex, player);
    }

    @Override
    public MatchingTeam cloneForNewLeague() {
        MatchingTeam clone = new MatchingTeam(globalTeam, leagueIndex, playerByName.firstEntry().getValue());
        clone.playerByName = new TreeMap<>(playerByName);
        return clone;
    }
}