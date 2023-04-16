package org.app.game_classes;

import java.util.TreeMap;

public class TreeTeam extends GenericMatchingTeam<TreeTeam> {
    // Gewinner-Team in der nächsten Liga (entweder dieses Team oder sein Gegner)
    private TreeTeam parent;

    // Dieses Team und das Team, gegen das es in der vorherigen Liga gewonnen hat
    private TreeTeam child1;
    private TreeTeam child2;

    public TreeTeam(GlobalTeam globalTeam, int leagueIndex) {
        super(globalTeam, leagueIndex);
        this.parent = null;
        this.child1 = null;
        this.child2 = null;
    }

    @Override
    public TreeTeam cloneForNewLeague() {
        TreeTeam clone = new TreeTeam(globalTeam, leagueIndex);
        clone.playerByName = new TreeMap<>(playerByName);
        return clone;
    }

    public TreeTeam getParent() {
        return parent;
    }
    public void setParent(TreeTeam parent) {
        this.parent = parent;
    }
    public TreeTeam getChild1() {
        return child1;
    }
    public void setChild1(TreeTeam child1) {
        this.child1 = child1;
    }
    public TreeTeam getChild2() {
        return child2;
    }
    public void setChild2(TreeTeam child2) {
        this.child2 = child2;
    }
    public boolean isFinished() {
        return parent != null;
    }

    @Override
    public String toString() {
        String info = ((PlayingTeam) this).toString() + ", Gegner: " + opponent.getName() + ", ";
        if (isFinished()) {
            if (match.isTotalDraw()) info += "Unentschieden.";
            else if (isTotalWinner()) info += "Dieses Team führt insgesamt.";
            else info += "Dieses Team hat verloren.";
        } else info += "Gewinner: noch nicht festgelegt.";
        return info;
    }
}