package org.app.game_classes;

import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import jakarta.persistence.*;

/** GlobalTeam enthält die Eigenschaften eines Teams, die für alle Ligen gleich sind. */
@Entity
@Table(name = "global_team")
public class GlobalTeam {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    // Name des Teams
    private String name;

    @Column(name = "game_id")
    // ID des Spiels, zu dem das Team gehört
    private UUID gameID;

    @Column(name = "description")
    // Beschreibung des Teams
    private String description;

    @OneToMany(mappedBy = "globalTeam")
    @MapKey(name = "name")
    // enthält alle Spieler, die für dieses Team spielen
    private TreeMap<String, Player> playerByName;

    public GlobalTeam() {}
    public GlobalTeam(String name, UUID gameID, Player player) {
        this.name = name;
        this.gameID = gameID;

        this.description = "";
        playerByName = new TreeMap<>();
        addPlayer(player);
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
}