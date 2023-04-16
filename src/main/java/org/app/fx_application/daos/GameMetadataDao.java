package org.app.fx_application.daos;

import java.util.List;
import java.util.UUID;

import org.app.GameMetadata;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.*;
import org.jdbi.v3.sqlobject.config.*;

public interface GameMetadataDao {
    @RegisterConstructorMapper(GameMetadata.class)
    @SqlQuery("""
    SELECT game.id, :type type, game.name, game.num_suffix, game.description, wl.assigned_role, wl.assigned_role wl_role, game.public_view, game.solo_teams,
    game.allow_own_teams_creation FROM game
    INNER JOIN game_whitelist wl ON wl.game_id = game.id AND wl.account_name = :accountName
    WHERE gametype = :type
    UNION
    SELECT game.id, :type type, name, num_suffix, description, 1, 0, 1, solo_teams, allow_own_teams_creation FROM game
    WHERE gametype = :type AND public_view = 1
    AND NOT EXISTS (SELECT 1 FROM game_whitelist wl WHERE wl.game_id = game.id AND wl.account_name = :accountName)
    ORDER BY assigned_role, name, num_suffix""")
    List<GameMetadata> accessibleGames(String accountName, byte type);

    default GameMetadata getMetadataById(UUID id, String accountName) {
        GameMetadata metadata = getWhitelistedMetadataById(id, accountName);
        return metadata != null ? metadata : getPublicMetadataById(id);
    }
    @RegisterConstructorMapper(GameMetadata.class)
    @SqlQuery("""
    SELECT :id AS id, gametype type, game.name, game.num_suffix, game.description, wl.assigned_role, wl.assigned_role wl_role,
    game.public_view, game.solo_teams, game.allow_own_teams_creation FROM game
    INNER JOIN game_whitelist wl ON wl.game_id = game.id AND wl.account_name = :accountName
    WHERE game.id = :id""")
    GameMetadata getWhitelistedMetadataById(UUID id, String accountName);
    @RegisterConstructorMapper(GameMetadata.class)
    @SqlQuery("""
    SELECT :id AS id, gametype type, name, num_suffix, description, 1 assigned_role, 0 wl_role, 1 AS public_view, solo_teams, allow_own_teams_creation FROM game
    WHERE public_view = 1 AND id = :id""")
    GameMetadata getPublicMetadataById(UUID id);

    @SqlUpdate("UPDATE game SET name = :getName, num_suffix = :getNumSuffix, description = :getDescription, " +
            "public_view = :isPublicView, allow_own_teams_creation = :isAllowOwnTeamsCreation " +
            "WHERE id = :getId")
    void updateGameMetadata(@BindMethods GameMetadata metadata);
}