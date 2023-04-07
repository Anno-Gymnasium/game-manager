package org.app.game_classes;

import java.util.Random;

/** Match speichert den Stand von zwei gegeneinander spielenden Teams. */
public class Match<T extends GenericMatchingTeam<T>> {
    private final T team1;
    private final T team2;

    // Punktzahlen von Team 1 und Team 2 nach finaler Auswertung des Matches
    private int team1FinalScore;
    private int team2FinalScore;

    public Match(T team1, T team2) {
        this.team1 = team1;
        this.team2 = team2;
        team1FinalScore = -1;
        team2FinalScore = -1;
    }
    public void addScore(T team, int diff) {
        if (!(team.equals(team1) || team.equals(team2))) {
            return;
        }
        team.addMatchScore(diff);
    }
    public T flipACoin() {
        Random coin = new Random();
        if (coin.nextBoolean()) {
            team1.addTotalScore(1);
            return team1;
        }
        team2.addTotalScore(1);
        return team2;
    }
    public boolean isMatchDraw() {
        return team1.getMatchScore() == team2.getMatchScore();
    }
    public boolean isTotalDraw() {
        return team1.getTotalScore() == team2.getTotalScore();
    }

    public void reset() {
        team1FinalScore = -1;
        team2FinalScore = -1;
        team1.resetMatchScore();
        team2.resetMatchScore();
    }

    public T getTeam1() {
        return team1;
    }
    public T getTeam2() {
        return team2;
    }

    public T getMatchWinner() {
        if (team1FinalScore > team2FinalScore) {
            return team1;
        }
        if (team2FinalScore > team1FinalScore) {
            return team2;
        }
        return null;
    }
    public T getTotalWinner() {
        if (team1.getTotalScore() > team2.getTotalScore()) {
            return team1;
        }
        if (team2.getTotalScore() > team1.getTotalScore()) {
            return team2;
        }
        return null;
    }

    public void setFinalScores(int team1FinalScore, int team2FinalScore) {
        this.team1FinalScore = team1FinalScore;
        this.team2FinalScore = team2FinalScore;
    }
    public int getTeam1FinalScore() {
        return team1FinalScore;
    }
    public int getTeam2FinalScore() {
        return team2FinalScore;
    }
    public boolean isEvaluated() {
        return team1FinalScore > -1 || team2FinalScore > -1;
    }
}