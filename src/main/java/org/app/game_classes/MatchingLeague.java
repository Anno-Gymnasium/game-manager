package org.app.game_classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.app.game_classes.GenericMatchingGame.MatchEvaluationMode.*;
import static org.app.game_classes.GenericMatchingGame.RematchMode.*;
/**  */
public class MatchingLeague<T extends GenericMatchingTeam<T>> extends GenericLeague<T> {
    /**
     * Wird beim Erstellen von Matches geworfen, wenn
     *  */
    public static class MatchMakingException extends Exception {
        public MatchMakingException(String errorMessage) {
            super(errorMessage);
        }
        public MatchMakingException() {
            super("Matches können nicht erstellt werden, gerade Anzahl von über 0 Teams erforderlich.");
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

    // Log der Matches/Runden, die in dieser Liga gespielt wurden
    protected List<LoggedMatchGeneration<T>> matchLog;

    public MatchingLeague(Class<? extends T> teamClass) {
        super(teamClass);
        matches = new ArrayList<>();
        this.evaluationMode = ADD_ONE_FOR_WINNER;
        this.rematchMode = EVALUATE_CURRENT_MATCHES;
        matchesEvaluated = false;
        matchLog = new ArrayList<>();
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
        if (nTeams % 2 == 1 || nTeams == 0) {
            throw new MatchMakingException();
        }

        switch (rematchMode) {
            case EVALUATE_CURRENT_MATCHES -> evaluateMatches();
            case RESET_ALL_SCORES -> resetTotalScores();
        }

        resetMatchScores();
        matchesEvaluated = false;

        // Fall 1: Es gab vorher keine Matches oder nur eines
        if (matches.isEmpty() || nTeams == 2) {
            List<T> teams = new ArrayList<>(teamByName.values());
            Collections.shuffle(teams);
            for (int i = 0; i < nTeams; i += 2) {
                matches.add(new Match<>(teams.get(i), teams.get(i + 1)));
                teams.get(i).setOpponent(teams.get(i + 1));
                teams.get(i + 1).setOpponent(teams.get(i));
            }
            return;
        }

        // Fall 2: Es gab vorher mehr als ein Match. Dann werden neue Matches so erstellt, dass keines wie ein vorheriges Match ist.
        List<T> teamsFromMatches = new ArrayList<>();
        for (Match<T> match : matches) {
            teamsFromMatches.add(match.getTeam1());
            teamsFromMatches.add(match.getTeam2());
        }
        matches.clear();
        List<T> teamsFromFirstHalfMatches = teamsFromMatches.subList(0, nTeams / 2);
        List<T> teamsFromSecondHalfMatches = teamsFromMatches.subList(nTeams / 2, nTeams);

        Collections.shuffle(teamsFromFirstHalfMatches);
        Collections.shuffle(teamsFromSecondHalfMatches);
        for (int i = 0; i < nTeams / 2; i++) {
            T team1 = teamsFromFirstHalfMatches.get(i);
            T team2 = teamsFromSecondHalfMatches.get(0);
            if (team1.getOpponent().equals(team2)) {
                team2 = teamsFromSecondHalfMatches.get(1);
            }
            teamsFromSecondHalfMatches.remove(team2);
            matches.add(new Match<>(team1, team2));
            team1.setOpponent(team2);
            team2.setOpponent(team1);
        }
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
        if (matchesEvaluated || matches.isEmpty()) return;
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
        matchLog.add(new LoggedMatchGeneration<>(matches));
        matchesEvaluated = true;
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
    public List<LoggedMatchGeneration<T>> getMatchLog() {
        return matchLog;
    }

    public boolean matchesEvaluated() {
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