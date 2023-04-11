package org.app.game_classes;

import java.util.Objects;
import java.util.UUID;

public class WhiteListEntry {
    private String accountName;
    private UUID gameId;
    private byte assignedRole;

    public WhiteListEntry(String accountName, UUID gameId, byte assignedRole) {
        this.accountName = accountName;
        this.gameId = gameId;
        this.assignedRole = assignedRole;
    }

    public byte getAssignedRole() {
        return assignedRole;
    }
    public String getAccountName() {
        return accountName;
    }
    public UUID getGameId() {
        return gameId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WhiteListEntry other = (WhiteListEntry) o;
        return Objects.equals(accountName, other.accountName) &&
                Objects.equals(gameId, other.gameId);
    }
    @Override
    public int hashCode() {
        return Objects.hash(accountName, gameId);
    }
}