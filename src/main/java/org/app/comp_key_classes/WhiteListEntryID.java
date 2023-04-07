package org.app.comp_key_classes;

import java.io.Serializable;
import jakarta.persistence.*;

import java.util.Objects;
import java.util.UUID;

@Embeddable
public class WhiteListEntryID implements Serializable {
    private String accountName;
    private UUID gameId;

    public WhiteListEntryID() {}
    public WhiteListEntryID(String accountName, UUID gameId) {
        this.accountName = accountName;
        this.gameId = gameId;
    }

    public String getAccountName() {
        return accountName;
    }
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public UUID getGameId() {
        return gameId;
    }
    public void setGameId(UUID gameId) {
        this.gameId = gameId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WhiteListEntryID other = (WhiteListEntryID) o;
        return Objects.equals(accountName, other.accountName) &&
               Objects.equals(gameId, other.gameId);
    }
    @Override
    public int hashCode() {
        return Objects.hash(accountName, gameId);
    }
}