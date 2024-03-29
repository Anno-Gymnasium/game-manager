package org.app.game_classes;

import org.app.GameMetadata;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/** Superklasse von den Spielarten. */
public abstract class GenericGame<T extends PlayingTeam, L extends GenericLeague<T>> {
    public static final int MAX_NAME_LENGTH = 25;
    public static final int MAX_DESCRIPTION_LENGTH = 120;
    public static final int MAX_TEAMNAME_LENGTH = 20;
    public static final int MAX_TEAM_DESCRIPTION_LENGTH = 70;
    public static final int MAX_PLAYERNAME_LENGTH = 20;
    public static final int MAX_PLAYER_DESCRIPTION_LENGTH = 70;

    public static final int ADMIN_PING_TIMEOUT_SECONDS = 10;
    public static final int ADMIN_PING_INTERVAL_SECONDS = 8;

    // enthält alle globalen Teams, die in diesem Spiel spielen
    protected TreeMap<String, GlobalTeam> teamByName;

    // Liste der Ligen und aktuelle Liga
    protected List<L> leagues;

    protected L currentLeague;

    // enthält alle Spieler, die in diesem Spiel spielen
    protected TreeSet<Player> allPlayers;

    // enthält für jeweils einen Account-Namen den zugehörigen Spieler (falls vorhanden)
    protected HashMap<String, Player> accountPlayerBindings;

    // ID des Spiels
    protected UUID id;
    // Wenn true, dann wird jedem Spieler ein eigenes Team zugewiesen
    protected boolean soloTeams;
    // Wenn true, dann kann jeder Account das Spiel ansehen, ansonsten nach Whitelist
    protected boolean publicView;
    // Wenn true, dann kann jeder Spieler neue Teams erstellen, ansonsten nur Admins
    protected boolean allowOwnTeamsCreation;

    protected GameMetadata metadata;

    public GenericGame(GameMetadata metadata) {
        leagues = new ArrayList<>();
        currentLeague = null;

        this.metadata = metadata;

        this.id = metadata.getId();
        this.soloTeams = metadata.isSoloTeams();
        this.publicView = metadata.isPublicView();
        this.allowOwnTeamsCreation = metadata.isAllowOwnTeamsCreation();

        accountPlayerBindings = new HashMap<>();
        teamByName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        allPlayers = new TreeSet<>(Player::compareTo);
    }

    protected void checkForDuplicatePlayer(@NotNull Player player) throws DuplicatePlayerException {
        if (allPlayers.contains(player)) {
            throw new DuplicatePlayerException();
        }
    }
    protected void checkForDuplicateTeamname(@NotNull String teamName) throws DuplicateTeamnameException {
        if (teamByName.containsKey(teamName)) {
            throw new DuplicateTeamnameException();
        }
    }

    public void bindPlayerToAccount(@NotNull Player player, @NotNull Account account) {
        player = allPlayers.ceiling(player);
        if (player == null) {
            throw new IllegalArgumentException("Spieler ist nicht in diesem Spiel registriert.");
        }
        accountPlayerBindings.put(account.getName(), player);
        player.setAccount(account);
    }
    public void removeAccountBinding(@NotNull String accountName) {
        accountPlayerBindings.get(accountName).setAccount(null);
        accountPlayerBindings.remove(accountName);
    }
    public void removeAccountBinding(@NotNull Player player) {
        accountPlayerBindings.remove(player.getAccount().getName());
        player.setAccount(null);
    }

    public void addPlayer(@NotNull Player player) throws DuplicatePlayerException {
        if (soloTeams) {
            player.setGlobalTeam(new GlobalTeam(player.getName(), id));
        }
        checkForDuplicatePlayer(player);
        String teamName = player.getGlobalTeam().getName();
        GlobalTeam team = teamByName.get(teamName);
        if (team == null) {
            team = player.getGlobalTeam();
            teamByName.put(teamName, team);
        } else if (!soloTeams) {
            player.setGlobalTeam(team);
            team.addPlayer(player);
        }
        allPlayers.add(player);
    }
    public void addToCurrentLeague(@NotNull Player player) throws DuplicatePlayerException {
        addPlayer(player);
        currentLeague.addPlayer(player, player.getGlobalTeam());
    }
    public Player getPlayer(@NotNull String playerName, @NotNull String teamName) {
        return allPlayers.ceiling(new Player(playerName, new GlobalTeam(teamName, id, null)));
    }
    public List<Player> searchPlayers(@NotNull String playerName) {
        if (soloTeams) {
            Player player = getPlayer(playerName, playerName);
            if (player != null) {
                return List.of(player);
            }
            return List.of();
        }
        List<Player> result = new ArrayList<>();
        Player current = new Player(playerName, new GlobalTeam(playerName, id, null));
        while (current != null && current.getName().equalsIgnoreCase(playerName)) {
            current = allPlayers.higher(current);
            if (current != null) {
                result.add(current);
            }
        }

        return result;
    }

    public void removePlayerFromCurrentLeague(@NotNull Player player) {
        currentLeague.removePlayer(player);
    }
    public void removeTeamFromCurrentLeague(@NotNull GlobalTeam team) {
        currentLeague.removeTeam(team.getName());
    }

    public boolean hasTeamPlayed(@NotNull String teamName) {
        if (! teamByName.containsKey(teamName)) return false;
        for (L league : leagues.subList(0, leagues.size() - 1)) {
            if (league.hasTeam(teamName)) {
                return true;
            }
        }
        return false;
    }
    public boolean hasPlayerPlayed(@NotNull Player player) {
        if (! allPlayers.contains(player)) return false;
        for (L league : leagues.subList(0, leagues.size() - 1)) {
            if (league.hasPlayer(player)) {
                return true;
            }
        }
        return false;
    }
    public void removeTeamFromGame(@NotNull String teamName) throws HasPlayedException {
        GlobalTeam team = teamByName.get(teamName);
        if (team == null) return;
        if (hasTeamPlayed(team.getName())) {
            throw new HasPlayedException();
        }
        for (Player player : team.getPlayers()) {
            allPlayers.remove(player);
        }
        teamByName.remove(teamName);
        removeTeamFromCurrentLeague(team);
    }
    public void removePlayerFromGame(@NotNull Player player) throws HasPlayedException {
        if (soloTeams) removeTeamFromGame(player.getName());

        if (hasPlayerPlayed(player)) {
            throw new HasPlayedException();
        }
        allPlayers.remove(player);
        GlobalTeam team = teamByName.get(player.getGlobalTeam().getName());
        if (team != null) {
            team.removePlayer(player);
        }
        removePlayerFromCurrentLeague(player);
    }

    public void renamePlayer(@NotNull Player player, @NotNull String newName) throws DuplicatePlayerException, DuplicateTeamnameException {
        GlobalTeam team = teamByName.get(player.getGlobalTeam().getName());
        if (team == null) return;
        if (!allPlayers.contains(player)) return;

        if (soloTeams) {
            renameTeam(player.getGlobalTeam().getName(), newName);
        }
        checkForDuplicatePlayer(new Player(newName, player.getGlobalTeam()));
        player.setName(newName);
        team.removePlayer(player);
        team.addPlayer(player);
    }
    public void renameTeam(@NotNull String oldName, @NotNull String newName) throws DuplicateTeamnameException {
        if (oldName.equalsIgnoreCase(newName) || soloTeams) return;

        checkForDuplicateTeamname(newName);
        GlobalTeam team = teamByName.get(oldName);
        if (team == null) return;
        team.setName(newName);
        teamByName.remove(oldName);
        teamByName.put(newName, team);
    }
    public void changePlayerTeam(@NotNull Player player, @NotNull String newTeamName) throws DuplicatePlayerException {
        if (soloTeams) return;

        GlobalTeam team = teamByName.get(player.getGlobalTeam().getName());
        if (team == null) return;
        if (!allPlayers.contains(player)) return;

        checkForDuplicatePlayer(new Player(player.getName(), new GlobalTeam(newTeamName, id, null)));
        team.removePlayer(player);

        team = teamByName.get(newTeamName);
        if (team == null) {
            team = new GlobalTeam(newTeamName, id);
            team.addPlayer(player);
            teamByName.put(newTeamName, team);
        } else {
            team.addPlayer(player);
        }
        player.setGlobalTeam(team);
    }

    public TreeMap<String, GlobalTeam> getTeamByName() {
        return teamByName;
    }
    public TreeSet<Player> getAllPlayers() {
        return allPlayers;
    }
    public boolean isSoloTeams() {
        return soloTeams;
    }

    public GlobalTeam getWinnerTeam() {
        return currentLeague.getWinnerTeam().getGlobalTeam();
    }
    public List<List<T>> getRankings() {
        return currentLeague.getRankings();
    }
    public HashMap<T, Integer> getRankingMap() {
        return currentLeague.getRankingMap();
    }

    public List<T> getCurrentLeagueTeams() {
        return currentLeague.getTeams();
    }
    public L getCurrentLeague() {
        return currentLeague;
    }
    public List<L> getLeagues() {
        return leagues;
    }

    public void addTotalScore(GlobalTeam team, int score) {
        currentLeague.addTotalScore(team.getName(), score);
    }

    public GameMetadata getMetadata() {
        return metadata;
    }

    public void updatePublicView() {
        this.publicView = metadata.isPublicView();
    }
    public boolean isPublicView() {
        return publicView;
    }
    public void updateAllowOwnTeamsCreation() {
        this.allowOwnTeamsCreation = metadata.isAllowOwnTeamsCreation();
    }
    public boolean isAllowOwnTeamsCreation() {
        return allowOwnTeamsCreation;
    }

    public UUID getId() {
        return id;
    }

    /** Wird ausgelöst, wenn ein hinzuzufügender Spieler bereits existiert. */
    public static class DuplicatePlayerException extends DuplicateException {
        public DuplicatePlayerException(String errorMessage) {
            super(errorMessage);
        }
        public DuplicatePlayerException() {
            super("Spieler existiert bereits.");
        }
    }
    /** Wird ausgelöst, wenn ein hinzuzufügender Teamname bereits existiert. */
    public static class DuplicateTeamnameException extends DuplicateException {
        public DuplicateTeamnameException(String errorMessage) {
            super(errorMessage);
        }
        public DuplicateTeamnameException() {
            super("Teamname existiert bereits.");
        }
    }
    /** Wird ausgelöst, wenn ein zu löschendes Team oder ein Spieler bereits in einer alten Liga gespielt hat. */
    public static class HasPlayedException extends Exception {
        public HasPlayedException(String errorMessage) {
            super(errorMessage);
        }
        public HasPlayedException() {
            super("Team oder Spieler, das/der gelöscht werden soll, hat bereits in einer alten Liga gespielt.");
        }
    }
}