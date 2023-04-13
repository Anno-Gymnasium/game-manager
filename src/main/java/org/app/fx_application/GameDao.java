package org.app.fx_application;

import org.app.fx_application.selectables.PreviewGame;
import org.app.game_classes.GenericGame;

import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.*;

import java.util.UUID;

public interface GameDao {
    @SqlQuery("SELECT COUNT(1) FROM game WHERE name = :gameName")
    int countGamesWithName(String gameName);

    @SqlQuery("SELECT admin_online FROM game WHERE id = :gameId")
    boolean isAdminOnline(UUID gameId);

    @SqlUpdate("INSERT INTO game(id, name, num_suffix, description, solo_teams, public_view, allow_own_teams_creation)" +
            " VALUES (:getId, :getName, :numSuffix, :getDescription, :isSoloTeams, :isPublicView, :isAllowOwnTeamsCreation)")
    void insertAsGenericGame(@BindMethods GenericGame<?, ?> game, int numSuffix);
    @SqlUpdate("INSERT INTO matchless_game VALUES (:getId)")
    void insertAsMatchlessGame(@BindMethods GenericGame<?, ?> game);
    @SqlUpdate("INSERT INTO generic_matching_game VALUES (:getId)")
    void insertAsGenericMatchingGame(@BindMethods GenericGame<?, ?> game);
    @SqlUpdate("INSERT INTO matching_game VALUES (:getId)")
    void insertAsMatchingGame(@BindMethods GenericGame<?, ?> game);
    @SqlUpdate("INSERT INTO tree_game VALUES (:getId)")
    void insertAsTreeGame(@BindMethods GenericGame<?, ?> game);

    @SqlUpdate("UPDATE game SET name = :getName, num_suffix = :numSuffix, description = :getDescription, " +
                "public_view = :isPublicView, allow_own_teams_creation = :isAllowOwnTeamsCreation " +
                "WHERE id = :getId")
    void updateGamePreview(@BindMethods GenericGame<?, ?> game, int numSuffix);

    @SqlUpdate("UPDATE game SET name = :getGameName, num_suffix = :getGameNumSuffix, description = :getGameDescription, " +
                "public_view = :isPublicView, allow_own_teams_creation = :isAllowOwnTeamsCreation " +
                "WHERE id = :getGameId")
    void updateGamePreview(@BindMethods PreviewGame game);
}