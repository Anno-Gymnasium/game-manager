package org.app.game_classes;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.HashMap;

import org.jdbi.v3.core.mapper.reflect.ColumnName;
import org.springframework.security.crypto.bcrypt.BCrypt;

public class Account {
    // Name und Beschreibung des Accounts
    private String name;
    private String description;

    // Erstellungsdatum des Accounts
    private final Timestamp dateCreated;

    // E-Mail-Adresse des Accounts
    private String email;

    // BCCrypt-Hash des Passworts
    private String passwordHash;

    // Ob der Account von anderen zu Spielen hinzugefügt werden darf
    private boolean allowPassiveGameJoining;

    private HashMap<UUID, WhiteListEntry> accessibleGames = new HashMap<>();

    public Account(String name, String description, @ColumnName("pw_hash") String passwordHash, String email,
                   @ColumnName("date_created") Timestamp dateCreated,
                   @ColumnName("allow_passive_game_joining") boolean allowPassiveGameJoining) {
        this.name = name;
        this.description = description;
        this.passwordHash = passwordHash;
        this.email = email;
        this.dateCreated = dateCreated;
        this.allowPassiveGameJoining = allowPassiveGameJoining;
    }
    public static Account fromRawPassword(String name, String password, String email, Timestamp dateCreated, boolean allowPassiveGameJoining) {
        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
        return new Account(name, "", passwordHash, email, dateCreated, allowPassiveGameJoining);
    }

    public boolean authenticate(String password) {
        return BCrypt.checkpw(password, passwordHash);
    }

    public byte getRole(UUID gameID) {
        return accessibleGames.get(gameID).assignedRole();
    }
    public void addEntry(WhiteListEntry entry) {
        accessibleGames.put(entry.gameId(), entry);
    }
    public void updatePassword(String newPassword) {
        passwordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPasswordHash() {
        return passwordHash;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Timestamp getDateCreated() {
        return dateCreated;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setAllowPassiveGameJoining(boolean allowPassiveGameJoining) {
        this.allowPassiveGameJoining = allowPassiveGameJoining;
    }
    public boolean isAllowPassiveGameJoining() {
        return allowPassiveGameJoining;
    }

    @Override
    public String toString() {
        return "Account: " + name + " (" + email + ")";
    }
}