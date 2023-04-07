package org.app.game_classes;

import jakarta.persistence.*;
import org.app.comp_key_classes.WhiteListEntryID;

import java.util.UUID;

@Entity
@Table(name = "game_whitelist")
public class WhiteListEntry {
    @EmbeddedId
    private WhiteListEntryID id = new WhiteListEntryID();

    @ManyToOne
    @MapsId("accountName")
    private Account account;

    // @ManyToOne
    // @MapsId("gameId")
    // TODO: Später wieder auf GenericGame umstellen (auskommentieren)
    //private GenericGame<?, ?> game;
    // private GenericMatchingGame game;

    @Column(name = "assigned_role")
    private byte assignedRole;

    public void setAssignedRole(byte role) {
        this.assignedRole = role;
    }
    public byte getAssignedRole() {
        return assignedRole;
    }
    public Account getAccount() {
        return account;
    }
}