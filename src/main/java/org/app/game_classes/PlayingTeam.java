package org.app.game_classes;

import java.util.*;

public class PlayingTeam implements Comparable<PlayingTeam> {
    protected UUID id;

    // GlobalTeam, zu dem dieses Team gehört
    protected GlobalTeam globalTeam;

    protected int leagueIndex;

    // Punktzahl des Teams in der aktuellen Liga
    protected int totalScore;

    // Spieler, die in diesem Team spielen
    protected TreeMap<String, Player> playerByName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public PlayingTeam() {}
    public PlayingTeam(GlobalTeam globalTeam, int leagueIndex, Player player) {
        this.leagueIndex = leagueIndex;
        this.globalTeam = globalTeam;
        this.totalScore = 0;
        addPlayer(player);
    }
    public void addPlayer(Player player) {
        playerByName.put(player.getName(), player);
    }
    public void removePlayer(Player player) {
        playerByName.remove(player.getName());
    }
    public List<Player> getPlayers() {
        return new ArrayList<>(playerByName.values());
    }
    public int getSize() {
        return playerByName.size();
    }

    public void addTotalScore(int diff) {
        totalScore = Math.max(0, totalScore + diff);
    }
    public int getTotalScore() {
        return totalScore;
    }
    public void resetTotalScore() {
        totalScore = 0;
    }

    public GlobalTeam getGlobalTeam() {
        return globalTeam;
    }
    public String getName() {
        return globalTeam.getName();
    }

    public PlayingTeam cloneForNewLeague() {
        PlayingTeam clone = new PlayingTeam(globalTeam, leagueIndex, playerByName.firstEntry().getValue());
        clone.playerByName = new TreeMap<>(playerByName);
        return clone;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof PlayingTeam otherTeam)) {
            return false;
        }
        return Objects.equals(globalTeam, otherTeam.globalTeam) &&
                Objects.equals(leagueIndex, otherTeam.leagueIndex);
    }
    @Override
    public String toString() {
        return "Teamname: " + getName() + ", Punktzahl: " + getTotalScore() +
                ", Anzahl Spieler: " + getPlayers().size();
    }
    @Override
    public int compareTo(PlayingTeam other) {
        int globalTeamCompare = globalTeam.compareTo(other.globalTeam);
        if (globalTeamCompare != 0) {
            return globalTeamCompare;
        }
        return Integer.compare(leagueIndex, other.leagueIndex);
    }
}