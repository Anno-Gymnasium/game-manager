package org.app.fx_application;

import java.util.List;

import org.app.fx_application.selectables.PreviewGame;
import org.jdbi.v3.sqlobject.statement.*;
import org.jdbi.v3.sqlobject.config.*;

public interface GameSelectionDao {
    @RegisterConstructorMapper(PreviewGame.class)
    @SqlQuery("""
    SELECT game.id, 0 type, game.name, game.num_suffix, game.description, wl.assigned_role, wl.assigned_role wl_role, game.public_view, game.solo_teams,
    game.allow_own_teams_creation FROM game INNER JOIN
    matchless_game ON game.id = matchless_game.id
    INNER JOIN game_whitelist wl ON wl.game_id = game.id AND wl.account_name = :accountName
    UNION
    SELECT game.id, 0 type, name, num_suffix, description, 0, -1, 1, solo_teams, allow_own_teams_creation FROM game
    INNER JOIN matchless_game ON game.id = matchless_game.id WHERE public_view = 1
    AND NOT EXISTS (SELECT 1 FROM game_whitelist wl WHERE wl.game_id = game.id AND wl.account_name = :accountName)
    ORDER BY assigned_role, name, num_suffix""")
    List<PreviewGame> accessibleMatchlessGames(String accountName);

    @RegisterConstructorMapper(PreviewGame.class)
    @SqlQuery("""
    SELECT game.id, 1 type, game.name, game.num_suffix, game.description, wl.assigned_role, wl.assigned_role wl_role, game.public_view, game.solo_teams,
    game.allow_own_teams_creation FROM game INNER JOIN
    matching_game ON game.id = matching_game.id
    INNER JOIN game_whitelist wl ON wl.game_id = game.id AND wl.account_name = :accountName
    UNION
    SELECT game.id, 1 type, name, num_suffix, description, 0, -1, 1, solo_teams, allow_own_teams_creation FROM game
    INNER JOIN matching_game ON game.id = matching_game.id WHERE public_view = 1
    AND NOT EXISTS (SELECT 1 FROM game_whitelist wl WHERE wl.game_id = game.id AND wl.account_name = :accountName)
    ORDER BY assigned_role, name, num_suffix""")
    List<PreviewGame> accessibleMatchingGames(String accountName);

    @RegisterConstructorMapper(PreviewGame.class)
    @SqlQuery("""
    SELECT game.id, 2 type, game.name, game.num_suffix, game.description, wl.assigned_role, wl.assigned_role wl_role, game.public_view, game.solo_teams,
    game.allow_own_teams_creation FROM game INNER JOIN
    tree_game ON game.id = tree_game.id
    INNER JOIN game_whitelist wl ON wl.game_id = game.id AND wl.account_name = :accountName
    UNION
    SELECT game.id, 2 type, name, num_suffix, description, 0, -1, 1, solo_teams, allow_own_teams_creation FROM game
    INNER JOIN tree_game ON game.id = tree_game.id WHERE public_view = 1
    AND NOT EXISTS (SELECT 1 FROM game_whitelist wl WHERE wl.game_id = game.id AND wl.account_name = :accountName)
    ORDER BY assigned_role, name, num_suffix""")
    List<PreviewGame> accessibleTreeGames(String accountName);
}