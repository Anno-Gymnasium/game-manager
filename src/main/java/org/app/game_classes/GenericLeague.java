package org.app.game_classes;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.Comparator;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.HashMap;

import jakarta.persistence.*;

// @Entity
// @Table(name = "generic_league")
// @Inheritance(strategy = InheritanceType.JOINED)
public abstract class GenericLeague<T extends PlayingTeam> {
    // enthält alle Teams, die in dieser Liga aktiv sind
    protected TreeMap<String, T> teamByName;

    // enthält alle Spieler, die in dieser Liga aktiv sind
    protected HashSet<Player> leaguePlayers;

    @Transient
    // Konstruktor für Teams des Typs T
    private final Constructor<? extends T> teamConstructor;

    private List<T> teamsSortedByScore;
    private boolean sortedByScoreValid;

    public GenericLeague() {
        teamConstructor = null;
    }
    public GenericLeague(Class<? extends T> teamClass) {
        try {
            teamConstructor = teamClass.getConstructor(GlobalTeam.class, Player.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        teamByName = new TreeMap<>();
        leaguePlayers = new HashSet<>();
        teamsSortedByScore = new ArrayList<>();
        sortedByScoreValid = false;
    }

    public boolean hasPlayer(@NotNull Player player) {
        return leaguePlayers.contains(player);
    }
    public boolean hasTeam(@NotNull String teamName) {
        return teamByName.containsKey(teamName);
    }
    public void addPlayer(@NotNull Player player, GlobalTeam globalTeam) throws GenericGame.DuplicatePlayerException {
        if (hasPlayer(player)) {
            throw new GenericGame.DuplicatePlayerException();
        }
        String teamName = player.getGlobalTeam().getName();
        T team = teamByName.get(teamName);
        if (team == null) {
            try {
                team = teamConstructor.newInstance(globalTeam, player);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            teamByName.put(teamName, team);
            sortedByScoreValid = false;
        } else team.addPlayer(player);
        leaguePlayers.add(player);
    }
    public void addTeam(@NotNull T team) {
        if (teamByName.containsValue(team)) return;
        teamByName.put(team.getName(), team);
        leaguePlayers.addAll(team.getPlayers());
        sortedByScoreValid = false;
    }
    public void removePlayer(@NotNull Player player) {
        String teamName = player.getGlobalTeam().getName();
        if (hasTeam(teamName)) {
            T team = teamByName.get(teamName);
            team.removePlayer(player);
            if (team.getSize() == 0) {
                teamByName.remove(teamName);
            }
        }
        leaguePlayers.remove(player);
    }
    public void removeTeam(@NotNull String teamName) {
        if (hasTeam(teamName)) {
            T team = teamByName.get(teamName);
            team.getPlayers().forEach(leaguePlayers::remove);
            teamByName.remove(teamName);
        }
    }

    public void addTotalScore(String teamName, int score) {
        T playingTeam = teamByName.get(teamName);
        if (playingTeam != null) {
            playingTeam.addTotalScore(score);
        }
        sortedByScoreValid = false;
    }

    public List<T> getTeams() {
        return new ArrayList<>(teamByName.values());
    }
    public List<T> getTeamsSortedByScore(boolean ascending) {
        if (sortedByScoreValid) return teamsSortedByScore;

        List<T> teams = new ArrayList<>(teamByName.values());
        Comparator<T> comp = Comparator.comparingInt(T::getTotalScore);
        if (!ascending) {
            comp = comp.reversed();
        }
        teams.sort(comp);
        sortedByScoreValid = true;
        return teams;
    }
    public T getWinnerTeam() {
        if (teamByName.size() == 1) {
            return teamByName.firstEntry().getValue();
        }
        return null;
    }

    public List<List<T>> getRankings() {
        List<List<T>> rankings = new ArrayList<>();
        List<T> teams = getTeamsSortedByScore(false);
        int lastScore = -1;
        List<T> currentRanking = null;
        for (T team : teams) {
            int score = team.getTotalScore();
            if (score != lastScore) {
                currentRanking = new ArrayList<>();
                rankings.add(currentRanking);
                lastScore = score;
            }
            assert currentRanking != null;
            currentRanking.add(team);
        }
        return rankings;
    }
    public HashMap<T, Integer> getRankingMap() {
        HashMap<T, Integer> rankingMap = new HashMap<>();
        List<List<T>> rankings = getRankings();
        for (int i = 0; i < rankings.size(); i++) {
            for (T team : rankings.get(i)) {
                rankingMap.put(team, i + 1);
            }
        }
        return rankingMap;
    }
}