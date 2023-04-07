package org.app.game_classes;

import java.util.UUID;
import java.util.List;

public abstract class GenericMatchingGame<T extends GenericMatchingTeam<T>, L extends MatchingLeague<T>> extends GenericGame<T, L> {
    public enum MatchEvaluationMode {
        ADD_ONE_FOR_WINNER,
        ADD_MATCH_SCORES
    }
    public enum RematchMode {
        EVALUATE_CURRENT_MATCHES,
        DISCARD_CURRENT_MATCHES,
        RESET_ALL_SCORES
    }
    public static class NotEvaluatedException extends Exception {
        public NotEvaluatedException(String message) {
            super(message);
        }
        public NotEvaluatedException() {
            super("Die Matches wurden noch nicht ausgewertet.");
        }
    }

    protected MatchEvaluationMode evaluationMode;
    protected RematchMode rematchMode;
    protected boolean skipDrawsOnEvaluation;

    public GenericMatchingGame(boolean soloTeams, UUID id) {
        super(soloTeams, id);
        evaluationMode = MatchEvaluationMode.ADD_ONE_FOR_WINNER;
        rematchMode = RematchMode.EVALUATE_CURRENT_MATCHES;
        skipDrawsOnEvaluation = false;
    }
    public GenericMatchingGame(boolean soloTeams) {
        this(soloTeams, UUID.randomUUID());
    }

    public void generateShuffledMatches() throws MatchingLeague.MatchDrawException, MatchingLeague.MatchMakingException {
        currentLeague.generateRandomMatches();
    }
    public void newMatchRounds() throws MatchingLeague.MatchDrawException {
        currentLeague.newMatchRounds();
    }
    public void evaluateMatches() throws MatchingLeague.MatchDrawException {
        currentLeague.evaluateMatches();
    }

    public void setEvaluationMode(MatchEvaluationMode evaluationMode) {
        this.evaluationMode = evaluationMode;
        currentLeague.setEvaluationMode(evaluationMode);
    }
    public MatchEvaluationMode getEvaluationMode() {
        return evaluationMode;
    }
    public void setRematchMode(RematchMode rematchMode) {
        this.rematchMode = rematchMode;
        currentLeague.setRematchMode(rematchMode);
    }
    public RematchMode getRematchMode() {
        return rematchMode;
    }

    public void skipDrawsOnEvaluation(boolean skipDrawsOnEvaluation) {
        this.skipDrawsOnEvaluation = skipDrawsOnEvaluation;
        currentLeague.skipDrawsOnEvaluation(skipDrawsOnEvaluation);
    }
    public boolean getSkipDrawsOnEvaluation() {
        return skipDrawsOnEvaluation;
    }
    public boolean matchesEvaluated() {
        return currentLeague.MatchesEvaluated();
    }

    public List<Match<T>> getMatches() {
        return currentLeague.getMatches();
    }
    public List<Match<T>> getCurrentMatches() {
        return currentLeague.getMatches();
    }
}