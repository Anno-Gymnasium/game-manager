package org.app.comp_key_classes;

import java.io.Serializable;
import jakarta.persistence.*;

import java.util.Objects;
import java.util.UUID;

@Embeddable
public class CompPlayingTeamID implements Serializable {
    private int leagueIndex;
    private UUID globalTeamID;

    public CompPlayingTeamID() {
    }
    public CompPlayingTeamID(int leagueIndex, UUID globalTeamID) {
        this.leagueIndex = leagueIndex;
        this.globalTeamID = globalTeamID;
    }

    public int getLeagueIndex() {
        return leagueIndex;
    }
    public void setLeagueIndex(int leagueIndex) {
        this.leagueIndex = leagueIndex;
    }
    public UUID getGlobalTeamID() {
        return globalTeamID;
    }
    public void setGlobalTeamID(UUID globalTeamID) {
        this.globalTeamID = globalTeamID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompPlayingTeamID other = (CompPlayingTeamID) o;
        return Objects.equals(leagueIndex, other.leagueIndex) &&
               Objects.equals(globalTeamID, other.globalTeamID);
    }
    @Override
    public int hashCode() {
        return Objects.hash(leagueIndex, globalTeamID);
    }
}