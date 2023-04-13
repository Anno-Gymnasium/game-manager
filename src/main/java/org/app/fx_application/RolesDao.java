package org.app.fx_application;

import org.jdbi.v3.sqlobject.statement.*;
import org.jdbi.v3.sqlobject.config.*;

import java.util.UUID;

public interface RolesDao {
    @SqlQuery("IF EXISTS (SELECT * FROM game_whitelist WHERE game_id = :gameId AND account_name = :accountName) SELECT 1 ELSE SELECT 0")
    boolean isWhitelisted(UUID gameId, String accountName);

    default void setWhitelistRole(UUID gameId, String accountName, byte role) {
        if (isWhitelisted(gameId, accountName)) {
            updateWhitelistRole(gameId, accountName, role);
        } else {
            insertWhitelistRole(gameId, accountName, role);
        }
    }

    @SqlUpdate("INSERT INTO game_whitelist (game_id, account_name, assigned_role) VALUES (:gameId, :accountName, :role)")
    void insertWhitelistRole(UUID gameId, String accountName, byte role);

    @SqlUpdate("UPDATE game_whitelist SET assigned_role = :role WHERE game_id = :gameId AND account_name = :accountName")
    void updateWhitelistRole(UUID gameId, String accountName, byte role);
}