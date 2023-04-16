package org.app.game_classes;

import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

/** GlobalTeam enthält die Eigenschaften eines Teams, die für das gesamte Spiel gelten. */
public class GlobalTeam implements Comparable<GlobalTeam> {
    private UUID id;

    // Name des Teams
    private String name;

    // ID des Spiels, zu dem das Team gehört
    private UUID gameID;

    // Beschreibung des Teams
    private String description;

    // enthält alle Spieler, die für dieses Team spielen
    private TreeMap<String, Player> playerByName;

    public GlobalTeam(String name, UUID gameID, UUID id) {
        this.name = name;
        this.gameID = gameID;

        this.description = "";
        playerByName = new TreeMap<>();
    }
    public GlobalTeam(String name, UUID gameID) {
        this(name, gameID, UUID.randomUUID());
    }

    public UUID getId() {return id;}
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {return name;}
    public void setDescription(String description) {
        this.description = description;
    }
    public String getDescription() {return description;}
    public UUID getGameID() {return gameID;}

    public void addPlayer(Player player) {
        playerByName.put(player.getName(), player);
    }
    public void removePlayer(Player player) {
        playerByName.remove(player.getName());
    }
    public List<Player> getPlayers() {
        return new ArrayList<>(playerByName.values());
    }
    public int getSize() {
        return playerByName.size();
    }

    public Player getPlayer(String name) {
        return playerByName.get(name);
    }
    public boolean hasPlayer(Player player) {
        return playerByName.get(player.getName()).equals(player);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof GlobalTeam otherTeam))return false;

        return otherTeam.getName().equals(name) && otherTeam.getGameID().equals(gameID);
    }
    @Override
    public String toString() {
        return "Teamname: " + name + ", Anzahl Spieler: " + playerByName.size();
    }
    @Override
    public int compareTo(GlobalTeam other) {
        int result = name.compareTo(other.name);
        if (result == 0) {
            result = gameID.compareTo(other.gameID);
        }
        return result;
    }
}