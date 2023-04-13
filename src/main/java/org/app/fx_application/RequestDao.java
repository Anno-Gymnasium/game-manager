package org.app.fx_application;

import org.app.fx_application.selectables.SelectableOutgoingRequest;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.*;

import java.util.List;
import java.util.UUID;

public interface RequestDao {
    @SqlUpdate("INSERT INTO game_whitelisting_request (account_name, game_id, requested_role, message) VALUES (:getAccountName, " +
            "(SELECT id FROM game WHERE name = :getGameName AND num_suffix = :getGameNumSuffix), :getRole.getValue, :getMessage)")
    void insertRequest(@BindMethods SelectableOutgoingRequest request);

    @SqlUpdate("DELETE FROM game_whitelisting_request WHERE account_name = :getAccountName AND game_id =" +
            "(SELECT game_id FROM game WHERE name = :getGameName AND num_suffix = :getGameNumSuffix)")
    void deleteRequest(@BindMethods SelectableOutgoingRequest request);

    @RegisterConstructorMapper(SelectableOutgoingRequest.class)
    @SqlQuery("SELECT wr.account_name accountName, game.name gameName, game.num_suffix gameNumSuffix, wr.requested_role role, wr.message " +
            "FROM game_whitelisting_request wr " +
            "INNER JOIN game ON wr.game_id = game.id " +
            "WHERE wr.account_name = :accountName")
    List<SelectableOutgoingRequest> getOutgoingRequests(String accountName);

    @SqlQuery("IF EXISTS (SELECT 1 FROM game_whitelisting_request WHERE account_name = :accountName AND game_id = :gameId) " +
            "SELECT 1 ELSE SELECT 0")
    boolean existsRequest(String accountName, UUID gameId);
}