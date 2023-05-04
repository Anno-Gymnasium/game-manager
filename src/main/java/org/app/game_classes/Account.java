package org.app.game_classes;

import java.time.LocalDate;

import org.jdbi.v3.core.mapper.reflect.ColumnName;
import org.springframework.security.crypto.bcrypt.BCrypt;

public class Account {
    public static final int MAX_NAME_LENGTH = 20;
    public static final int MAX_EMAIL_LENGTH = 50;
    public static final int MAX_DESCRIPTION_LENGTH = 100;

    // Name und Beschreibung des Accounts
    private String name;
    private String description;

    // Erstellungsdatum des Accounts
    private final LocalDate dateCreated;

    // E-Mail-Adresse des Accounts
    private String email;

    // BCCrypt-Hash des Passworts
    private String passwordHash;

    // Ob der Account von anderen zu Spielen hinzugefügt werden darf
    private boolean allowPassiveGameJoining;

    public Account(String name, String description, @ColumnName("pw_hash") String passwordHash, String email,
                   @ColumnName("date_created") LocalDate dateCreated,
                   @ColumnName("allow_passive_game_joining") boolean allowPassiveGameJoining) {
        this.name = name;
        this.description = description;
        this.passwordHash = passwordHash;
        this.email = email;
        this.dateCreated = dateCreated;
        this.allowPassiveGameJoining = allowPassiveGameJoining;
    }
    public static Account fromRawPassword(String name, String password, String email, LocalDate dateCreated, boolean allowPassiveGameJoining) {
        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
        return new Account(name, "", passwordHash, email, dateCreated, allowPassiveGameJoining);
    }

    public boolean authenticate(String password) {
        return BCrypt.checkpw(password, passwordHash);
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
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getPasswordHash() {
        return passwordHash;
    }
    public LocalDate getDateCreated() {
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