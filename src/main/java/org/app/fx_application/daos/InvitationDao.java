package org.app.fx_application.daos;

import org.app.fx_application.selectables.SelectableInvitation;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.*;

import java.util.List;
import java.util.UUID;

public interface InvitationDao {
    @SqlQuery("IF EXISTS (SELECT 1 FROM game_invitation WHERE account_name = :accountName AND game_id = :gameId) " +
            "SELECT 1 ELSE SELECT 0")
    boolean existsInvitation(UUID gameId, String accountName);

    @SqlQuery("SELECT TOP 100 name FROM account WHERE name != :invitingAccountName AND name LIKE :query + '%' AND name NOT IN " +
            "(SELECT account_name FROM game_whitelist WHERE game_id = :gameId AND assigned_role = 3) ORDER BY name")
    List<String> getInvitableAccountNamesByQuery(UUID gameId, String invitingAccountName, String query);

    @RegisterConstructorMapper(SelectableInvitation.class)
    @SqlQuery("SELECT TOP 100 game.id gameId, game.name gameName, wl.assigned_role newRole, gi.message " +
            "FROM game_invitation gi " +
            "INNER JOIN game_whitelist wl ON wl.game_id = gi.game_id AND wl.account_name = gi.account_name " +
            "INNER JOIN game ON game.id = gi.game_id " +
            "WHERE gi.account_name = :accountName " +
            "ORDER BY game.name")
    List<SelectableInvitation> getInvitations(String accountName);

    @SqlUpdate("INSERT INTO game_invitation VALUES (:gameId, :accountName, :message)")
    void insertInvitation(UUID gameId, String accountName, String message);
    @SqlUpdate("UPDATE game_invitation SET message = :message WHERE game_id = :gameId AND account_name = :accountName")
    void updateInvitation(UUID gameId, String accountName, String message);

    default void setInvitation(UUID gameId, String accountName, String message) {
        if (existsInvitation(gameId, accountName)) {
            updateInvitation(gameId, accountName, message);
        } else {
            insertInvitation(gameId, accountName, message);
        }
    }

    @SqlUpdate("DELETE FROM game_invitation WHERE game_id = :gameId AND account_name = :accountName")
    void deleteInvitation(UUID gameId, String accountName);
}