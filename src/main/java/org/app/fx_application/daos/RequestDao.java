package org.app.fx_application.daos;

import org.app.fx_application.selectables.SelectableRequest;
import org.app.fx_application.selectables.SelectableIncomingRequest;
import org.app.fx_application.selectables.SelectableOutgoingRequest;

import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.*;

import java.util.List;
import java.util.UUID;

public interface RequestDao {
    @SqlUpdate("INSERT INTO game_whitelisting_request (account_name, game_id, requested_role, message) VALUES (:getAccountName, " +
            "(SELECT id FROM game WHERE name = :getGameName AND num_suffix = :getGameNumSuffix), :getRequestedRole.getValue, :getMessage)")
    void insertRequest(@BindMethods SelectableRequest request);

    @SqlUpdate("DELETE FROM game_whitelisting_request WHERE account_name = :getAccountName AND game_id =" +
            "(SELECT game_id FROM game WHERE name = :getGameName AND num_suffix = :getGameNumSuffix)")
    void deleteRequest(@BindMethods SelectableRequest request);

    @SqlQuery("IF EXISTS (SELECT 1 FROM game_whitelisting_request WHERE account_name = :accountName AND game_id = :gameId) " +
            "SELECT 1 ELSE SELECT 0")
    boolean existsRequest(String accountName, UUID gameId);

    @SqlUpdate("UPDATE game_whitelisting_request SET accepted_role = :role WHERE account_name = :getAccountName AND game_id =" +
            "(SELECT game_id FROM game WHERE name = :getGameName AND num_suffix = :getGameNumSuffix)")
    void acceptRequest(@BindMethods SelectableRequest request, byte role);

    @SqlUpdate("UPDATE game_whitelisting_request SET accepted_role = 4 WHERE account_name = :getAccountName AND game_id =" +
            "(SELECT game_id FROM game WHERE name = :getGameName AND num_suffix = :getGameNumSuffix)")
    void rejectRequest(@BindMethods SelectableRequest request);

    @RegisterConstructorMapper(SelectableIncomingRequest.class)
    @SqlQuery("""
        SELECT wr.account_name accountName, game.id gameId, game.name gameName, game.num_suffix gameNumSuffix,
        ISNULL((SELECT assigned_role FROM game_whitelist WHERE game_id = game.id AND account_name = wr.account_name), 0) currentRole,
        wr.requested_role requestedRole, wr.message

        FROM game_whitelisting_request wr
        INNER JOIN game ON wr.game_id = game.id

        WHERE (SELECT assigned_role FROM game_whitelist WHERE game_id = game.id AND account_name = :receivingAccountName) = 3
        AND wr.accepted_role IS NULL""") // Empfänger muss Admin sein und die Anfrage muss noch unbeantwortet sein (accepted_role = NULL)
    List<SelectableIncomingRequest> getIncomingRequests(String receivingAccountName);

    @RegisterConstructorMapper(SelectableOutgoingRequest.class)
    @SqlQuery("SELECT wr.account_name accountName, game.name gameName, game.num_suffix gameNumSuffix, wr.requested_role requestedRole, wr.message," +
            "NULL acceptedRole, id gameId, gametype FROM game_whitelisting_request wr " +
            "INNER JOIN game ON wr.game_id = game.id " +
            "WHERE wr.account_name = :accountName AND wr.accepted_role IS NULL")
    List<SelectableOutgoingRequest> getPendingOutgoingRequests(String accountName);

    @RegisterConstructorMapper(SelectableOutgoingRequest.class)
    @SqlQuery("SELECT wr.account_name accountName, game.name gameName, game.num_suffix gameNumSuffix, wr.requested_role requestedRole, wr.message," +
            "wr.accepted_role acceptedRole, id gameId, gametype FROM game_whitelisting_request wr " +
            "INNER JOIN game ON wr.game_id = game.id " +
            "WHERE wr.account_name = :accountName AND wr.accepted_role IS NOT NULL")
    List<SelectableOutgoingRequest> getAnsweredOutgoingRequests(String accountName);
}