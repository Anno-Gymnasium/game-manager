create table game (
    id UNIQUEIDENTIFIER,
    gametype tinyint not null,
    name varchar(25) collate Latin1_General_CS_AS not null,
    num_suffix int not null,
    description varchar(120),
    solo_teams bit not null,
    public_view bit not null default 0, -- Legt fest, ob jeder als Viewer beitreten kann
    allow_own_teams_creation bit not null default 0, -- Legt fest, ob beitretende Spieler ihre eigenen Teams erstellen dürfen
    admin_online bit not null default 0, -- Wahr, wenn momentan ein Admin online ist und somit die anderen Admins nur als Spieler/Zuschauer das Spiel betreten dürfen

    primary key(id),
    constraint game_unique_name_with_suffix unique(name, num_suffix)
)

create table matchless_game (
    id UNIQUEIDENTIFIER,

    primary key(id),
    constraint matchless_game_fk_parent_id foreign key(id) references game(id) on delete cascade
)

create table generic_matching_game (
    id UNIQUEIDENTIFIER,

    primary key(id),
    constraint generic_matching_game_fk_parent_id foreign key(id) references game(id) on delete cascade
)

create table matching_game (
    id UNIQUEIDENTIFIER,

    primary key(id),
    constraint matching_game_fk_parent_id foreign key(id) references generic_matching_game(id) on delete cascade
)

create table tree_game (
    id UNIQUEIDENTIFIER,

    primary key(id),
    constraint tree_game_fk_parent_id foreign key(id) references generic_matching_game(id) on delete cascade
)

create table account (
    name varchar(20),
    description varchar(100) not null default '',
    email varchar(50) not null,
    date_created date not null default GETDATE(),
    last_login datetime not null default GETDATE(),
    pw_hash char(60) collate Latin1_General_CS_AS not null,
    allow_passive_game_joining bit not null default 0, -- Legt fest, ob der Account von jemand anderem zu einem Spiel hinzugefüft werden darf

    primary key(name),
    constraint account_unique_email unique(email)
)

create table global_team (
    id UNIQUEIDENTIFIER,
    name varchar(20),
    game_id UNIQUEIDENTIFIER,
    description varchar(70),
    use_joining_whitelist bit not null default 0,

    primary key(id),
    constraint global_team_unqiue_name_game_id unique (name, game_id),
    constraint global_team_fk_game_id foreign key(game_id) references game(id) on delete cascade
)

create table player (
    id UNIQUEIDENTIFIER,
    global_team_id UNIQUEIDENTIFIER,
    is_team_leader bit not null default 0,
    account_name varchar(20) default null,
    name varchar(20),
    description varchar(70),

    primary key(id),
    unique(name, global_team_id),
    constraint player_fk_global_team_id foreign key(global_team_id) references global_team(id) on delete cascade,
    constraint player_fk_account_name foreign key(account_name) references account(name) on delete set null on update cascade
)

create table league (
    [index] int,
    game_id UNIQUEIDENTIFIER,

    primary key(game_id, [index]),
    constraint league_fk_game_id foreign key(game_id) references game(id) on delete cascade
)

create function is_matchless_game_id (@id UNIQUEIDENTIFIER) returns bit AS
BEGIN
    if (exists(select 1 from matchless_game where matchless_game.id = @id))
        return 1
    return 0
END

create table matchless_league (
    [index] int,
    game_id UNIQUEIDENTIFIER,

    primary key(game_id, [index]),
    constraint matchless_league_fk_parent_game_id_and_index foreign key(game_id, [index]) references league(game_id, [index]) on delete cascade,

    -- (FK-Constraint nicht möglich) Prüft, ob die Game-ID einem Matchless-Game gehört
    constraint matchless_league_chk_fk_matchless_game_id check(dbo.is_matchless_game_id(game_id) = 1)
)

create function is_generic_matching_game_id (@id UNIQUEIDENTIFIER) returns bit AS
BEGIN
    if (exists(select 1 from generic_matching_game where generic_matching_game.id = @id))
        return 1
    return 0
END

create table matching_league (
    [index] int,
    game_id UNIQUEIDENTIFIER,

    primary key(game_id, [index]),
    constraint matching_league_fk_parent_game_id_and_index foreign key(game_id, [index]) references league(game_id, [index]) on delete cascade,
    
    -- (FK-Constraint nicht möglich) Prüft, ob die Game-ID einem Generic-Matching-Game gehört
    constraint matching_league_chk_fk_gen_matching_game_id check(dbo.is_generic_matching_game_id(game_id) = 1)
)

create table logged_match_generation (
    id UNIQUEIDENTIFIER,
    game_id UNIQUEIDENTIFIER,
    league_index int,
    [index] int,

    primary key(id),
    constraint lm_generation_unique_comp_key unique (game_id, league_index, [index]),
    constraint lm_generation_fk_league_game_id_and_index foreign key(game_id, league_index) references matching_league(game_id, [index]) on delete cascade
)

create table playing_team (
    id UNIQUEIDENTIFIER,
    game_id UNIQUEIDENTIFIER,
    global_team_id UNIQUEIDENTIFIER,
    league_index int not null,
    total_score int not null default 0,

    primary key(id),
    constraint playing_team_unique_gt_id_and_lg_index unique(global_team_id, league_index),

    -- WICHTIG!!! Durch eine "Diamond"-Beziehung kein Cascade Delete möglich. Wenn ein Spiel gelöscht werden soll, 
    -- müssen zuerst alle playing_teams mit dessen Game-ID gelöscht werden (siehe Trigger unten).
    constraint playing_team_fk_global_team_id foreign key(global_team_id) references global_team(id) on delete no action,
    constraint playing_team_fk_league_game_id_and_index foreign key(game_id, league_index) references league(game_id, [index]) on delete no action
)

create trigger trigger_delete_game
on game instead of delete
AS

delete playing_team from playing_team inner join deleted
on playing_team.game_id = deleted.id

delete game from game inner join deleted
on game.id = deleted.id

create function game_id_is_generic_matching_game_id (@playing_team_id UNIQUEIDENTIFIER) returns bit AS
BEGIN
    if (exists
    (select 1 from playing_team inner join
    generic_matching_game on playing_team.id = @playing_team_id AND playing_team.game_id = generic_matching_game.id))
        return 1
    return 0
END

create table generic_matching_team (
    id UNIQUEIDENTIFIER,
    match_score int not null default 0,

    primary key(id),
    constraint gen_matching_team_fk_parent_id foreign key(id) references playing_team(id) on delete cascade,
    
    -- Prüft, ob die Game-ID dieses Teams einem Generic-Matching-Game gehört
    constraint gen_matching_team_chk_gen_matching_game_id check(dbo.game_id_is_generic_matching_game_id(id) = 1)
)

create function game_id_is_matching_game_id (@playing_team_id UNIQUEIDENTIFIER) returns bit AS
BEGIN
    if (exists
    (select 1 from playing_team inner join
    matching_game on playing_team.id = @playing_team_id AND playing_team.game_id = matching_game.id))
        return 1
    return 0
END

create table matching_team (
    id UNIQUEIDENTIFIER,

    primary key(id),
    constraint matching_team_fk_parent_id foreign key(id) references generic_matching_team(id) on delete cascade,

    -- Prüft, ob die Game-ID dieses Teams einem Matching-Game gehört
    constraint matching_team_chk_matching_game_id check(dbo.game_id_is_matching_game_id(id) = 1)
)

create function game_id_is_tree_game_id (@playing_team_id UNIQUEIDENTIFIER) returns bit AS
BEGIN
    if (exists
    (select 1 from playing_team inner join
    tree_game on playing_team.id = @playing_team_id AND playing_team.game_id = tree_game.id))
        return 1
    return 0
END

create table tree_team (
    id UNIQUEIDENTIFIER,
    child1_id UNIQUEIDENTIFIER,
    child2_id UNIQUEIDENTIFIER,

    primary key(id),
    constraint tree_team_fk_inheritance_parent_id foreign key(id) references generic_matching_team(id) on delete cascade,
    constraint tree_team_fk_child1_id foreign key(child1_id) references tree_team(id) on delete no action,
    constraint tree_team_fk_child2_id foreign key(child2_id) references tree_team(id) on delete no action,

    -- Prüft, ob die Game-ID dieses Teams einem Tree-Game gehört
    constraint tree_team_chk_tree_game_id check(dbo.game_id_is_tree_game_id(id) = 1)
)

create function valid_player_deployment (@player_id UNIQUEIDENTIFIER, @playing_team_id UNIQUEIDENTIFIER) returns bit AS
BEGIN
    if (exists
    (select 1 from player inner join playing_team
    on (player.id = @player_id AND playing_team.id = @playing_team_id) AND
    player.global_team_id = playing_team.global_team_id))
        return 1
    return 0
END

create table player_deployment (
    player_id UNIQUEIDENTIFIER,
    playing_team_id UNIQUEIDENTIFIER,

    primary key(player_id, playing_team_id),
    constraint deployment_fk_player_id foreign key(player_id) references player(id),
    constraint deployment_fk_playing_team_id foreign key(playing_team_id) references playing_team(id),

    -- Prüft, ob der Spieler und das Playing-Team dieselbe global_team_id haben
    constraint deployment_chk_valid_deployment check(dbo.valid_player_deployment(player_id, playing_team_id) = 1)
)

create function valid_match_teams (@gmt_1_id UNIQUEIDENTIFIER, @gmt_2_id UNIQUEIDENTIFIER) returns bit AS
BEGIN
    if (@gmt_1_id != @gmt_2_id AND exists
    (select 1 from playing_team p1 inner join playing_team p2
    on (p1.id = @gmt_1_id AND p2.id = @gmt_2_id)
    AND p1.game_id = p2.game_id
    AND p1.league_index = p2.league_index))
        return 1
    return 0
END

create table match (
    gen_matching_team_1_id UNIQUEIDENTIFIER,
    gen_matching_team_2_id UNIQUEIDENTIFIER not null,
    final_score_1 int default null,
    final_score_2 int default null,
    logged_match_generation_id UNIQUEIDENTIFIER default null,

    primary key(gen_matching_team_1_id),
    constraint match_unique_gmt2_id unique(gen_matching_team_2_id),

    constraint match_fk_gm_team_1_id foreign key(gen_matching_team_1_id) references generic_matching_team(id) on delete no action,
    constraint match_fk_gm_team_2_id foreign key(gen_matching_team_2_id) references generic_matching_team(id) on delete no action,
    constraint match_fk_lm_generation_id foreign key(logged_match_generation_id) references logged_match_generation(id) on delete cascade,

    -- Prüft, ob die beiden Teams verschieden sind, aber dieselbe Game-ID und dieselbe Liga haben
    constraint match_chk_valid_teams check(dbo.valid_match_teams(gen_matching_team_1_id, gen_matching_team_2_id) = 1)
)

create table game_whitelist ( -- Welche Accounts welchen Spielen mit welcher Rolle beitreten dürfen (kann von Admins für andere Accounts geupdated werden)
    game_id UNIQUEIDENTIFIER,
    account_name varchar(20),
    assigned_role tinyint not null, -- 1 = Viewer, 2 = Player, 3 = Admin (0 ist als "Ausgeschlossen" reserviert, kommt in der Whitelist nicht vor)

    primary key(game_id, account_name),
    constraint game_joining_wl_fk_game_id foreign key(game_id) references game(id) on delete cascade,
    constraint game_joining_wl_fk_account_name foreign key(account_name) references account(name) on delete cascade on update cascade
)

create table game_whitelisting_request ( -- Anfragen auf eine bestimmte Rolle in einem Spiel
    account_name varchar(20), -- Account, der die Rolle anfragt
    game_id UNIQUEIDENTIFIER, -- Spiel, in dem der Account diese Rolle haben will
    requested_role tinyint not null, -- Rolle, die der Account anfragt
    message varchar(80) not null default '', -- Kurze Nachricht zum Kontext der Anfrage
    accepted_role tinyint default null, -- Rolle, die ein Admin des Spiels dem Account als Antwort zugewiesen hat
    -- (null = ausstehend, 1 = Viewer, 2 = Player, 3 = Admin, 4 = abgelehnt (Behält alte Rolle))

    primary key(account_name, game_id),
    constraint game_wl_request_fk_account_name foreign key(account_name) references account(name) on delete cascade on update cascade,
    constraint game_wl_request_fk_game_id foreign key(game_id) references game(id) on delete cascade
)

create table game_invitation ( -- Einladung an einen Account zu einem Spiel unter einer bestimmten Rolle (kann von Admins verfasst werden)
    game_id UNIQUEIDENTIFIER,
    account_name varchar(20),
    message varchar(70) not null default '',

    primary key(game_id, account_name),
    constraint game_invitation_fk_from_whitelist foreign key(game_id, account_name) references game_whitelist(game_id, account_name) on delete cascade on update cascade
)

create table team_joining_whitelist ( -- Wenn im Global-Team aktiviert, dürfen nur diese Accounts in das Team eintreten (Bearbeitung durch Team-Leader und Admins)
    global_team_id UNIQUEIDENTIFIER,
    account_name varchar(20),

    primary key(global_team_id, account_name),
    constraint team_joining_wl_fk_gt_id foreign key(global_team_id) references global_team(id) on delete cascade,
    constraint team_joining_wl_fk_account_name foreign key(account_name) references account(name) on delete cascade on update cascade
)