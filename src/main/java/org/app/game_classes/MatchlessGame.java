package org.app.game_classes;

import org.app.GameMetadata;

import java.util.List;
import java.util.ArrayList;

import java.util.Random;

public class MatchlessGame extends GenericGame<PlayingTeam, MatchlessLeague> implements WithCustomLimitedPassing {
    public MatchlessGame(GameMetadata metadata) {
        super(metadata);
        leagues.add(currentLeague = new MatchlessLeague());
    }

    public int createNextLeague(int minScore) throws TeamSelectionException {
        MatchlessLeague nextLeague = new MatchlessLeague();
        for (PlayingTeam team : currentLeague.getTeams()) {
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

    public void createNextLeague(int nPassingTeams, boolean decideOnDraw) throws TeamSelectionException {
        int nTeams = currentLeague.getTeams().size();
        if (nPassingTeams > nTeams) throw new TeamSelectionException(nPassingTeams);
        List<PlayingTeam> sortedByScore = currentLeague.getTeamsSortedByScore(false);
        List<PlayingTeam> passingTeams = new ArrayList<>();
        MatchlessLeague nextLeague = new MatchlessLeague();

        if (nPassingTeams > 0 && nPassingTeams < nTeams) {
            List<PlayingTeam> randomFromRemainingGroup = new ArrayList<>();
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
        List<PlayingTeam> sortedByScore = currentLeague.getTeamsSortedByScore(false);
        for (int i = 0; i < sortedByScore.size() - 1; i++) {
            if (sortedByScore.get(i).getTotalScore() != sortedByScore.get(i + 1).getTotalScore()) {
                passingTeamNumbers.add(i + 1);
            }
        }
        passingTeamNumbers.add(sortedByScore.size());
        return passingTeamNumbers;
    }
}