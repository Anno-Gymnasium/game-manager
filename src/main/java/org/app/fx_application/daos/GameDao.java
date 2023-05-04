package org.app.fx_application.daos;

import org.app.GameMetadata;

import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.*;

import java.util.UUID;

public interface GameDao {
    @SqlQuery("SELECT COUNT(1) FROM game WHERE name = :gameName")
    int countGamesWithName(String gameName);

    @SqlQuery("IF DATEADD(SECOND, :adminPingTimeoutSeconds, (SELECT last_admin_ping FROM game WHERE id = :gameId)) >= GETDATE() SELECT 1 ELSE SELECT 0")
    boolean isAdminOnline(UUID gameId, int adminPingTimeoutSeconds);

    @SqlUpdate("UPDATE game SET last_admin_ping = GETDATE() WHERE id = :gameId")
    void pingAdminOnline(UUID gameId);

    @SqlUpdate("INSERT INTO game(id, gametype, name, num_suffix, description, solo_teams, public_view, allow_own_teams_creation)" +
            " VALUES (:getId, :getType.getValue, :getName, :getNumSuffix, :getDescription, :isSoloTeams, :isPublicView, :isAllowOwnTeamsCreation)")
    void insertAsGenericGame(@BindMethods GameMetadata metadata);
    @SqlUpdate("INSERT INTO matchless_game VALUES (:getId)")
    void insertAsMatchlessGame(@BindMethods GameMetadata metadata);
    @SqlUpdate("INSERT INTO generic_matching_game VALUES (:getId)")
    void insertAsGenericMatchingGame(@BindMethods GameMetadata metadata);
    @SqlUpdate("INSERT INTO matching_game VALUES (:getId)")
    void insertAsMatchingGame(@BindMethods GameMetadata metadata);
    @SqlUpdate("INSERT INTO tree_game VALUES (:getId)")
    void insertAsTreeGame(@BindMethods GameMetadata metadata);

    default void insertGame(@BindMethods GameMetadata metadata) {
        insertAsGenericGame(metadata);
        switch (metadata.getType()) {
            case MATCHLESS -> insertAsMatchlessGame(metadata);
            case MATCHING -> {insertAsGenericMatchingGame(metadata); insertAsMatchingGame(metadata);}
            case TREE -> {insertAsGenericMatchingGame(metadata); insertAsTreeGame(metadata);}
        }
    }

    @SqlUpdate("DELETE FROM game WHERE id = :gameId")
    void deleteGame(UUID gameId);
}