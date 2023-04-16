package org.app;

import org.app.fx_application.daos.GameMetadataDao;
import org.app.game_classes.Account;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

import java.util.UUID;

import org.app.fx_application.JdbiProvider;
import org.jdbi.v3.core.Jdbi;

/** Enthält alle wichtigen Spieldaten außerhalb des eigentlichen Spiels, z.B. Id, Name, Beschreibung, Spieltyp und Einstellungen,
 * und die Daten in Bezug auf den aktuellen Account, z.B. die zugewiesene höchste Rolle des Accounts im Spiel. */
public class GameMetadata implements Comparable<GameMetadata> {
    private Account currentAccount = null;
    private final UUID gameId;
    private final GameType type;
    private String name;
    private int numSuffix;
    private String description;
    private GameRole accountRole;
    private byte whitelistedRole;

    private final boolean soloTeams;
    private boolean publicView;
    private boolean ownTeamsCreation;
    private Jdbi jdbi = JdbiProvider.getInstance().getJdbi();

    public GameMetadata(UUID id, int type, String name, @ColumnName("num_suffix") int numSuffix, String description,
                        @ColumnName("assigned_role") byte accountRole, @ColumnName("wl_role") byte whitelistedRole, @ColumnName("public_view") boolean publicView,
                        @ColumnName("solo_teams") boolean soloTeams, @ColumnName("allow_own_teams_creation") boolean ownTeamsCreation) {
        this.gameId = id;
        this.type = GameType.getType(type);
        this.name = name.strip();
        this.numSuffix = numSuffix;
        this.description = description;
        this.accountRole = GameRole.getRole(accountRole);
        this.whitelistedRole = whitelistedRole;
        this.publicView = publicView;
        this.soloTeams = soloTeams;
        this.ownTeamsCreation = ownTeamsCreation;
    }

    public UUID getId() {
        return gameId;
    }
    public GameType getType() {
        return type;
    }
    public String getName() {
        return name;
    }
    public int getNumSuffix() {
        return numSuffix;
    }
    public String getDescription() {
        return description;
    }
    public GameRole getAccountRole() {
        return accountRole;
    }
    public byte getWhitelistedRole() {
        return whitelistedRole;
    }
    public boolean isPublicView() {
        return publicView;
    }
    public boolean isSoloTeams() {
        return soloTeams;
    }
    public boolean isAllowOwnTeamsCreation() {
        return ownTeamsCreation;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setNumSuffix(int numSuffix) {
        this.numSuffix = numSuffix;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setAccountRole(GameRole accountRole) {
        this.accountRole = accountRole;
    }
    public void setWhitelistedRole(byte whitelistedRole) {
        this.whitelistedRole = whitelistedRole;
    }
    public void setPublicView(boolean publicView) {
        this.publicView = publicView;
    }
    public void setAllowOwnTeamsCreation(boolean ownTeamsCreation) {
        this.ownTeamsCreation = ownTeamsCreation;
    }

    public Account getAccount() {
        return currentAccount;
    }
    public void setAccount(Account account) {
        this.currentAccount = account;
    }

    public void update() {
        GameMetadata newMetadata = jdbi.withExtension(GameMetadataDao.class, dao -> dao.getMetadataById(gameId, currentAccount.getName()));
        this.name = newMetadata.name;
        this.numSuffix = newMetadata.numSuffix;
        this.description = newMetadata.description;
        this.accountRole = newMetadata.accountRole;
        this.whitelistedRole = newMetadata.whitelistedRole;
        this.publicView = newMetadata.publicView;
        this.ownTeamsCreation = newMetadata.ownTeamsCreation;
    }

    public String getCompositeName() {
        return getCompositeName(name, numSuffix);
    }
    public static String getCompositeName(String name, int numSuffix) {
        return name + (numSuffix > 0 ? (" #" + numSuffix) : "");
    }

    @Override
    public int compareTo(GameMetadata other) {
        // Sortierung: 1. nach accountRole, 2. nach name, 3. nach numSuffix
        int result = accountRole.compareTo(other.accountRole);
        if (result == 0) {
            result = name.compareTo(other.name);
            if (result == 0) {
                result = Integer.compare(numSuffix, other.numSuffix);
            }
        }
        return result;
    }
}