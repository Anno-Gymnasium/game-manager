package org.app.game_classes;

public abstract class GenericMatchingTeam <T extends GenericMatchingTeam<T>> extends PlayingTeam {
    // Gegner des Teams in der aktuellen Liga
    protected T opponent;

    // Match, dem das Team zugeteilt wurde
    protected Match<T> match;

    // Punktzahl des Teams im aktuellen Match
    protected int matchScore;

    public GenericMatchingTeam(GlobalTeam globalTeam, int leagueIndex, Player player) {
        super(globalTeam, leagueIndex, player);
        this.matchScore = 0;
        this.opponent = null;
    }

    public void setOpponent(T opponent) {
        this.opponent = opponent;
    }
    public T getOpponent() {
        return opponent;
    }

    public void setMatch(Match<T> match) {
        this.match = match;
    }
    public Match<T> getMatch() {
        return match;
    }

    public void addMatchScore(int diff) {
        matchScore = Math.max(0, matchScore + diff);
    }
    public int getMatchScore() {
        return matchScore;
    }
    public void resetMatchScore() {
        matchScore = 0;
    }

    public boolean isMatchDraw() {
        return match.isMatchDraw();
    }
    public boolean isTotalDraw() {
        return match.isTotalDraw();
    }
    public boolean isMatchWinner() {
        return match.getMatchWinner() == this;
    }
    public boolean isTotalWinner() {
        return match.getTotalWinner() == this;
    }

    @Override
    public String toString() {
        return super.toString();
    }
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}