package org.app.game_classes;

import java.util.UUID;

public class TreeGame extends GenericMatchingGame<TreeTeam, MatchingLeague<TreeTeam>> {
    public TreeGame(boolean soloTeams, UUID id) {
        super(soloTeams, id);
        rematchMode = RematchMode.RESET_ALL_SCORES;
    }
    public TreeGame(boolean soloTeams) {
        this(soloTeams, UUID.randomUUID());
    }

    public void createNextLeague() throws NotEvaluatedException {
        if (! matchesEvaluated()) throw new NotEvaluatedException();

        MatchingLeague<TreeTeam> nextLeague = new MatchingLeague<>(TreeTeam.class);
        for (Match<TreeTeam> match : currentLeague.getMatches()) {
            TreeTeam nextTreeTeam = match.getTotalWinner().cloneForNewLeague();

            match.getTeam1().setParent(nextTreeTeam);
            match.getTeam2().setParent(nextTreeTeam);
            nextTreeTeam.setChild1(match.getTeam1());
            nextTreeTeam.setChild2(match.getTeam2());

            nextLeague.addTeam(nextTreeTeam);
        }
        leagues.add(nextLeague);
        currentLeague = nextLeague;
    }

    public static int getRecommendedTeamNumber(int nLeagues) {
        return (int) Math.pow(2, nLeagues);
    }
}