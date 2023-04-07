package org.app.game_classes;

import org.app.comp_key_classes.CompPlayerID;

import jakarta.persistence.*;

import java.util.UUID;

/** Player enthält die Eigenschaften eines Spielers. */
@Entity
@Table(name = "player", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "global_team_id"})
})
public class Player implements Comparable<Player> {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    // Name des Spielers
    private String name;

    @ManyToOne
    @JoinColumn(name = "global_team_id", referencedColumnName = "id")
    // Team, zu dem der Spieler gehört
    private GlobalTeam globalTeam;

    @Column(name = "description")
    // Kurze Information über den Spieler
    private String description;

    @Column(name = "is_team_leader")
    private boolean isTeamLeader;

    @OneToOne
    @JoinColumn(name = "account_name", referencedColumnName = "name")
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
        return id.compareTo(other.id);
    }
    @Override
    public String toString() {
        return "Name: " + name + ", Team: " + globalTeam.getName();
    }
}