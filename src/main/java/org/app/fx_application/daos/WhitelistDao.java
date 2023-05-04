package org.app.fx_application.daos;

import org.app.fx_application.selectables.SelectableWhitelistEntry;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.*;

import java.util.List;
import java.util.UUID;

public interface WhitelistDao {
    @SqlQuery("IF EXISTS (SELECT * FROM game_whitelist WHERE game_id = :gameId AND account_name = :accountName) SELECT 1 ELSE SELECT 0")
    boolean isWhitelisted(UUID gameId, String accountName);

    @SqlQuery("SELECT ISNULL((SELECT assigned_role FROM game_whitelist wl " +
            " WHERE wl.game_id = :gameId AND wl.account_name = :accountName), 0) ")
    byte getWhitelistRole(UUID gameId, String accountName);

    @RegisterConstructorMapper(SelectableWhitelistEntry.class)
    @SqlQuery("SELECT game_id gameId, account_name accountName, assigned_role role FROM game_whitelist " +
            "WHERE game_id = :gameId AND account_name NOT IN (<excludedAccountNames>) ORDER BY role DESC, account_name")
    List<SelectableWhitelistEntry> getWhitelist(UUID gameId, @BindList String... excludedAccountNames);

    default void setEntry(UUID gameId, String accountName, byte role) {
        if (isWhitelisted(gameId, accountName)) {
            updateEntry(gameId, accountName, role);
        } else {
            insertEntry(gameId, accountName, role);
        }
    }

    @SqlUpdate("INSERT INTO game_whitelist (game_id, account_name, assigned_role) VALUES (:gameId, :accountName, :role)")
    void insertEntry(UUID gameId, String accountName, byte role);
    @SqlUpdate("UPDATE game_whitelist SET assigned_role = :role WHERE game_id = :gameId AND account_name = :accountName")
    void updateEntry(UUID gameId, String accountName, byte role);
    @SqlUpdate("DELETE FROM game_whitelist WHERE game_id = :gameId AND account_name IN (<accountNames>)")
    void removeEntries(UUID gameId, @BindList(onEmpty = BindList.EmptyHandling.NULL_STRING) String... accountNames);
}