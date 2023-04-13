package org.app.game_classes;

import java.util.Objects;
import java.util.UUID;

public record WhiteListEntry(String accountName, UUID gameId, byte assignedRole) {
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