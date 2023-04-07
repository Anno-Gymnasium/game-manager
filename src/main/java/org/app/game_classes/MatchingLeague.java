package org.app.game_classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.app.game_classes.GenericMatchingGame.MatchEvaluationMode.*;
import static org.app.game_classes.GenericMatchingGame.RematchMode.*;
/**  */
public class MatchingLeague<T extends GenericMatchingTeam<T>> extends GenericLeague<T> {
    public static class MatchMakingException extends Exception {
        public MatchMakingException(String errorMessage) {
            super(errorMessage);
        }
        public MatchMakingException() {
            super("Matches können nicht erstellt werden.");
        }
    }
    public static class MatchDrawException extends Exception {
        public MatchDrawException(String errorMessage) {
            super(errorMessage);
        }
        public MatchDrawException() {
            super("Nicht alle Matches haben einen Gewinner.");
        }
    }
    // enthält die Matches, die in dieser Liga gespielt werden
    protected ArrayList<Match<T>> matches;

    // entscheidet, wie Matches ausgewertet werden
    protected GenericMatchingGame.MatchEvaluationMode evaluationMode;
    // entscheidet, was bei einem Rematch mit den aktuellen Matches und Scores passiert
    protected GenericMatchingGame.RematchMode rematchMode;

    // gibt an, ob bei der Auswertung von Matches unentschiedene Matches übersprungen werden
    protected boolean skipDrawsOnEvaluation;

    // gibt an, ob die aktuellen Matches bereits ausgewertet wurden
    protected boolean matchesEvaluated;

    public MatchingLeague(Class<? extends T> teamClass) {
        super(teamClass);
        matches = new ArrayList<>();
        this.evaluationMode = ADD_ONE_FOR_WINNER;
        this.rematchMode = EVALUATE_CURRENT_MATCHES;
        matchesEvaluated = false;
    }

    public void resetTotalScores() {
        for (T team : teamByName.values()) {
            team.resetTotalScore();
        }
    }
    public void resetMatchScores() {
        for (T team : teamByName.values()) {
            team.resetMatchScore();
        }
    }

    public void generateRandomMatches() throws MatchDrawException, MatchMakingException {
        int nTeams = teamByName.size();
        if (nTeams % 2 != 0 || nTeams == 0) {
            throw new MatchMakingException("Matches können nicht erstellt werden, gerade Anzahl von" +
                    "Teams > 0 erforderlich.");
        }

        switch (rematchMode) {
            case EVALUATE_CURRENT_MATCHES -> evaluateMatches();
            case RESET_ALL_SCORES -> {
                resetTotalScores();
            }
        }

        resetMatchScores();
        matches.clear();
        List<T> teams = new ArrayList<>(teamByName.values());
        Collections.shuffle(teams);
        for (int i = 0; i < nTeams; i+=2) {
            T team1 = teams.get(i);
            T team2 = teams.get(i+1);
            Match<T> match = new Match<T>(team1, team2);
            team1.setOpponent(team2);
            team1.setMatch(match);
            team2.setOpponent(team1);
            team2.setMatch(match);
            matches.add(match);
        }
        matchesEvaluated = false;
    }
    /** Erstellt neue Matches mit denselben Gegnern wie in den aktuellen Matches. */
    public void newMatchRounds() throws MatchDrawException {
        switch (rematchMode) {
            case EVALUATE_CURRENT_MATCHES -> evaluateMatches();
            case RESET_ALL_SCORES -> {
                resetTotalScores();
            }
        }
        for (Match<T> match : matches) {
            match.reset();
        }
        matchesEvaluated = false;
    }

    public void evaluateMatches() throws MatchDrawException {
        if (!matchesEvaluated && !matches.isEmpty()) {
            for (T team : teamByName.values()) {
                if (team.getMatchScore() == team.getOpponent().getMatchScore()) {
                    if (skipDrawsOnEvaluation) continue;
                    throw new MatchDrawException("Nicht alle Matches haben einen Gewinner.");
                }
                switch (evaluationMode) {
                    case ADD_MATCH_SCORES -> team.addTotalScore(team.getMatchScore());
                    case ADD_ONE_FOR_WINNER -> {
                        if (team.isMatchWinner()) {
                            team.addTotalScore(1);
                        }
                    }
                }
                team.resetMatchScore();
            }
            matchesEvaluated = true;
        }
    }

    public void flipCoinsForDraws () {
        for (Match<T> match : matches) {
            if (match.isMatchDraw()) {
                match.flipACoin();
            }
        }
    }
    public List<Match<T>> getMatches() {
        return matches;
    }

    public boolean MatchesEvaluated() {
        return matchesEvaluated;
    }
    public void skipDrawsOnEvaluation(boolean skipDrawsOnEvaluation) {
        this.skipDrawsOnEvaluation = skipDrawsOnEvaluation;
    }

    public void setEvaluationMode(GenericMatchingGame.MatchEvaluationMode evaluationMode) {
        this.evaluationMode = evaluationMode;
    }
    public GenericMatchingGame.MatchEvaluationMode getEvaluationMode() {
        return evaluationMode;
    }
    public void setRematchMode(GenericMatchingGame.RematchMode rematchMode) {
        this.rematchMode = rematchMode;
    }
    public GenericMatchingGame.RematchMode getRematchMode() {
        return rematchMode;
    }
}