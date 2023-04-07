package org.app.game_classes;

import org.app.comp_key_classes.CompPlayingTeamID;

import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "playing_team", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"global_team_id", "league_index"})
})
@Inheritance(strategy = InheritanceType.JOINED)
public class PlayingTeam {
    @Id
    @GeneratedValue
    @Column(name = "id")
    protected UUID id;

    @ManyToOne
    @JoinColumn(name = "global_team_id")
    // GlobalTeam, zu dem dieses Team gehört
    protected GlobalTeam globalTeam;

    @Column(name = "global_team_id", insertable = false, updatable = false)
    protected UUID globalTeamID;
    @Column(name = "league_index")
    protected int leagueIndex;

    @Column(name = "total_score")
    // Punktzahl des Teams in der aktuellen Liga
    protected int totalScore;

    @ManyToMany
    @JoinTable(name = "playing_team_player",
            joinColumns = @JoinColumn(name = "playing_team_id"),
            inverseJoinColumns = @JoinColumn(name = "player_id"))
    @MapKey(name = "name")
    // Spieler, die in diesem Team spielen
    protected TreeMap<String, Player> playerByName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public PlayingTeam() {}
    public PlayingTeam(GlobalTeam globalTeam, int leagueIndex, Player player) {
        this.leagueIndex = leagueIndex;
        this.globalTeam = globalTeam;
        this.globalTeamID = globalTeam.getId();
        this.totalScore = 0;
        addPlayer(player);
    }
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

    public void addTotalScore(int diff) {
        totalScore = Math.max(0, totalScore + diff);
    }
    public int getTotalScore() {
        return totalScore;
    }
    public void resetTotalScore() {
        totalScore = 0;
    }

    public GlobalTeam getGlobalTeam() {
        return globalTeam;
    }
    public String getName() {
        return globalTeam.getName();
    }

    public PlayingTeam cloneForNewLeague() {
        PlayingTeam clone = new PlayingTeam(globalTeam, leagueIndex, playerByName.firstEntry().getValue());
        clone.playerByName = new TreeMap<>(playerByName);
        return clone;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof PlayingTeam otherTeam)) {
            return false;
        }
        return otherTeam.getName().equalsIgnoreCase(this.getName());
    }
    @Override
    public String toString() {
        return "Teamname: " + getName() + ", Punktzahl: " + getTotalScore() +
                ", Anzahl Spieler: " + getPlayers().size();
    }
}