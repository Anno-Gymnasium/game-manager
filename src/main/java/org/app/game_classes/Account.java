package org.app.game_classes;

import java.util.UUID;

import java.util.HashMap;

import org.springframework.security.crypto.bcrypt.BCrypt;

import jakarta.persistence.*;

@Entity
@Table(name = "account")
public class Account {
    @Id
    @Column(name = "name")
    // Name und Beschreibung des Accounts
    private String name;
    @Column(name = "description")
    private String description;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "pw_hash")
    // BCCrypt-Hash des Passworts
    private String passwordHash;

    // TODO: Später wieder auskommentieren
    // @OneToMany(mappedBy = "account")
    // @MapKey(name = "gameID")
    // Gibt für eine Spiel-ID den WhiteListEntry zurück
    // private HashMap<UUID, WhiteListEntry> accessibleGames = new HashMap<>();

    public Account() {}
    public Account(String name, String password, String email) {
        this.name = name;
        this.description = "";
        passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
        this.email = email;
    }

    @Transient
    public boolean authenticate(String password) {
        return BCrypt.checkpw(password, passwordHash);
    }

    // TODO: Später wieder auskommentieren
//    @Transient
//    public byte getRole(UUID gameID) {
//        return accessibleGames.get(gameID).getAssignedRole();
//    }
//    @Transient
//    public void setRole(UUID gameID, byte role) {
//        accessibleGames.get(gameID).setAssignedRole(role);
//    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Account: " + name + " (" + email + ")";
    }
}