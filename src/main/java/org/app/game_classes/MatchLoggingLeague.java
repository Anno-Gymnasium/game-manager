package org.app.game_classes;

import java.util.List;
import java.util.ArrayList;

import static org.app.game_classes.GenericMatchingGame.RematchMode.*;

public class MatchLoggingLeague extends MatchingLeague<MatchingTeam> {
    private List<LoggedMatchGeneration> matchLog;
    public MatchLoggingLeague() {
        super(MatchingTeam.class);
        matchLog = new ArrayList<>();
    }

    @Override
    public void evaluateMatches() throws MatchDrawException {
        super.evaluateMatches();
        if (rematchMode != EVALUATE_CURRENT_MATCHES) return;
        matchLog.add(new LoggedMatchGeneration(matches));
    }

    public List<LoggedMatchGeneration> getMatchLog() {
        return matchLog;
    }
}