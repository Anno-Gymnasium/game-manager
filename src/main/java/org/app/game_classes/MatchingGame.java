package org.app.game_classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class MatchingGame extends GenericMatchingGame<MatchingTeam, MatchLoggingLeague> {
    public MatchingGame(boolean soloTeams, UUID id) {
        super(soloTeams, id);
    }
    public MatchingGame(boolean soloTeams) {
        this(soloTeams, UUID.randomUUID());
    }

    /** Erstellt eine neue Liga, in der alle Teams der aktuellen Liga weiterkommen, die mindestens minScore Punkte haben.
     * Gibt die Anzahl der Teams zurück, die nicht weitergekommen sind. */
    public int createNextLeague(int minScore) throws NotEvaluatedException, TeamSelectionException {
        if (! matchesEvaluated()) throw new NotEvaluatedException();

        MatchLoggingLeague nextLeague = new MatchLoggingLeague();
        for (MatchingTeam team : currentLeague.getTeams()) {
            if (team.getTotalScore() >= minScore) {
                nextLeague.addTeam(team);
            }
        }
        int nDisqualified = currentLeague.getTeams().size() - nextLeague.getTeams().size();
        if (nDisqualified == currentLeague.getTeams().size()) {
            throw new TeamSelectionException("Es gibt keine Teams, die die Mindestpunktzahl von " + minScore + " erreicht haben.");
        }
        if (nDisqualified == 0) {
            throw new TeamSelectionException("Es können keine Teams disqualifiziert werden, " +
                    "da alle Teams die Mindestpunktzahl von " + minScore + " erreicht haben.");
        }

        leagues.add(nextLeague);
        currentLeague = nextLeague;

        return nDisqualified;
    }

    /** Erstellt eine neue Liga, in der die nPassingTeams besten Teams der aktuellen Liga weiterkommen.
     * Wenn in der nach Punktzahl absteigend sortierten Liste das Team am Index nPassingTeams - 1 nach sich noch andere
     * Teams mit gleicher Punktzahl hat, wird unterschieden:
     * - Ist decideOnDraw = true, so werden aus der unklaren letzten Gruppe einige Teams zufällig ausgewählt.
     * - Ist decideOnDraw = false, so wird eine TeamSelectionException geworfen. */
    public void createNextLeague(int nPassingTeams, boolean decideOnDraw) throws NotEvaluatedException, TeamSelectionException {
        if (! matchesEvaluated()) throw new NotEvaluatedException();

        int nTeams = currentLeague.getTeams().size();
        if (nPassingTeams > nTeams) throw new TeamSelectionException(nPassingTeams);
        List<MatchingTeam> sortedByScore = currentLeague.getTeamsSortedByScore(false);
        List<MatchingTeam> passingTeams = new ArrayList<>();
        MatchLoggingLeague nextLeague = new MatchLoggingLeague();

        if (nPassingTeams > 0 && nPassingTeams < nTeams) {
            List<MatchingTeam> randomFromRemainingGroup = new ArrayList<>();
            if (sortedByScore.get(nPassingTeams - 1).getTotalScore() == sortedByScore.get(nPassingTeams).getTotalScore()) {
                if (decideOnDraw) {
                    int lastGroupStart;
                    int lastGroupEnd;
                    for (lastGroupStart = nPassingTeams - 1; lastGroupStart > 0 && sortedByScore.get(lastGroupStart - 1).getTotalScore() == sortedByScore.get(lastGroupStart).getTotalScore(); lastGroupStart--);
                    for (lastGroupEnd = nPassingTeams; lastGroupEnd < nTeams - 1 && sortedByScore.get(lastGroupEnd + 1).getTotalScore() == sortedByScore.get(lastGroupEnd).getTotalScore(); lastGroupEnd++);

                    randomFromRemainingGroup = sortedByScore.subList(lastGroupStart, lastGroupEnd + 1);

                    Random random = new Random();
                    while (randomFromRemainingGroup.size() > nPassingTeams - lastGroupStart) {
                        randomFromRemainingGroup.remove(random.nextInt(randomFromRemainingGroup.size()));
                    }
                } else {
                    throw new TeamSelectionException(nPassingTeams);
                }
            }
            for (int i = 0; i < nPassingTeams; i++) {
                passingTeams.add(sortedByScore.get(i));
            }
            passingTeams.addAll(randomFromRemainingGroup);
        } else {
            passingTeams = new ArrayList<>(sortedByScore);
        }
        passingTeams.forEach(team -> {
            nextLeague.addTeam(team.cloneForNewLeague());
        });
        leagues.add(currentLeague = nextLeague);
    }
    public List<Integer> getPossibleNPassingTeams() {
        List<Integer> passingTeamNumbers = new ArrayList<>();
        List<MatchingTeam> sortedByScore = currentLeague.getTeamsSortedByScore(false);
        for (int i = 0; i < sortedByScore.size() - 1; i++) {
            if (sortedByScore.get(i).getTotalScore() != sortedByScore.get(i + 1).getTotalScore()) {
                passingTeamNumbers.add(i + 1);
            }
        }
        passingTeamNumbers.add(sortedByScore.size());
        return passingTeamNumbers;
    }
}