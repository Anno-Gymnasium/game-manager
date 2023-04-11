package org.app.game_classes;

import java.util.UUID;

/** Player enthält die Eigenschaften eines Spielers. */
public class Player implements Comparable<Player> {
    private UUID id;

    // Name des Spielers
    private String name;

    // Team, zu dem der Spieler gehört
    private GlobalTeam globalTeam;

    // Kurze Information über den Spieler
    private String description;

    private boolean isTeamLeader;

    // Account, der mit dem Spieler verknüpft ist
    private Account boundAccount;

    public Player() {}
    public Player(String name, GlobalTeam globalTeam) {
        this.globalTeam = globalTeam;
        this.name = name;
        this.description = "";
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public void setGlobalTeam(GlobalTeam globalTeam) {
        this.globalTeam = globalTeam;
    }
    public GlobalTeam getGlobalTeam() {
        return globalTeam;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }

    public void setAccount(Account account) {
        boundAccount = account;
    }
    public Account getAccount() {
        return boundAccount;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Player otherPlayer)) {
            return false;
        }
        return otherPlayer.id.equals(id);
    }
    @Override
    public int compareTo(Player other) {
        int globalTeamCompare = globalTeam.compareTo(other.globalTeam);
        if (globalTeamCompare == 0) {
            return name.compareTo(other.name);
        }
        return globalTeamCompare;
    }
    @Override
    public String toString() {
        return "Name: " + name + ", Team: " + globalTeam.getName();
    }
}