package org.app.comp_key_classes;

import java.io.Serializable;
import jakarta.persistence.*;

import java.util.Objects;
import java.util.UUID;

@Embeddable
public class CompPlayerID implements Serializable, Comparable<CompPlayerID> {
    private String name;
    private UUID globalTeamID;

    public CompPlayerID() {}
    public CompPlayerID(String name, UUID globalTeamID) {
        this.name = name;
        this.globalTeamID = globalTeamID;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
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

        CompPlayerID other = (CompPlayerID) o;
        return Objects.equals(name, other.name) &&
               Objects.equals(globalTeamID, other.globalTeamID);
    }
    @Override
    public int hashCode() {
        return Objects.hash(name, globalTeamID);
    }

    @Override
    public int compareTo(CompPlayerID other) {
        int result = name.compareTo(other.name);
        if (result == 0) {
            result = globalTeamID.compareTo(other.globalTeamID);
        }
        return result;
    }
}