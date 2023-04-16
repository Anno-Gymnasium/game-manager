package org.app.fx_application.daos;

import org.app.GameMetadata;

import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.*;

import java.util.UUID;

public interface GameDao {
    @SqlQuery("SELECT COUNT(1) FROM game WHERE name = :gameName")
    int countGamesWithName(String gameName);

    @SqlQuery("SELECT admin_online FROM game WHERE id = :gameId")
    boolean isAdminOnline(UUID gameId);

    @SqlUpdate("UPDATE game SET admin_online = :adminOnline WHERE id = :gameId")
    void setAdminOnline(UUID gameId, boolean adminOnline);

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