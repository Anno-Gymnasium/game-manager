package org.app.game_classes;

import org.app.GameMetadata;

public class TreeGame extends GenericMatchingGame<TreeTeam, MatchingLeague<TreeTeam>> {
    public TreeGame(GameMetadata metadata) {
        super(metadata);
        rematchMode = RematchMode.RESET_ALL_SCORES;
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