package org.app.fx_application.controllers;

import javafx.collections.ListChangeListener;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.fxml.FXML;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.app.GameMetadata;
import org.app.GameType;
import org.app.fx_application.*;
import org.app.fx_application.daos.*;

import org.app.fx_application.dialogs.AccountSettingsDialog;
import org.app.fx_application.dialogs.GameCreationDialog;
import org.app.fx_application.selectables.GamePreview;
import org.app.game_classes.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jdbi.v3.core.Jdbi;

public class MainMenuController {
    public static EventType<Event> RELOAD = new EventType<>("RELOAD");

    @FXML private TextField gameSearchField, numSuffixField;
    @FXML private CheckBox cbPublicViewFilter, cbSoloTeamsFilter, cbOwnTeamsCreationFilter;
    @FXML private CheckBox cbAdminFilter, cbPlayerFilter, cbViewerFilter;
    @FXML private Button bReload, bAccountSettings, bLogout, bSearchGame, bReverseSort;
    @FXML private VBox vbMatchlessGames, vbMatchingGames, vbTreeGames;
    @FXML private VBox vbOutgoingRequests, vbIncomingRequests, vbRequestAnswers, vbGameInvitations;
    @FXML private Pane newIncomingRequestsPane, newRequestAnswersPane, newInvitationsPane;

    private HashMap<UUID, GameMetadata> gameMetadataById = new HashMap<>();
    private Jdbi jdbi = JdbiProvider.getInstance().getJdbi();
    private List<GamePreview> matchlessGames, matchingGames, treeGames;
    private Account currentAccount = null;
    private boolean sortAscending = true;

    public void initialize() {
        numSuffixField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                numSuffixField.setText(oldValue);
            }
            if (newValue.startsWith("0")) {
                numSuffixField.setText("0");
            }
        });

        // Listener für neue Anfragen, Antworten und Einladungen
        vbIncomingRequests.getChildren().addListener((ListChangeListener<Node>) c -> newIncomingRequestsPane.setVisible(!vbIncomingRequests.getChildren().isEmpty()));
        vbRequestAnswers.getChildren().addListener((ListChangeListener<Node>) c -> newRequestAnswersPane.setVisible(!vbRequestAnswers.getChildren().isEmpty()));
        vbGameInvitations.getChildren().addListener((ListChangeListener<Node>) c -> newInvitationsPane.setVisible(!vbGameInvitations.getChildren().isEmpty()));
    }

    @FXML
    public void onSearchFieldEnter() {
        bSearchGame.fire();
    }
    public void filterGames() {
        vbMatchlessGames.getChildren().clear();
        vbMatchingGames.getChildren().clear();
        vbTreeGames.getChildren().clear();
        matchlessGames.stream()
                .filter(this::filterGame)
                .forEach(game -> vbMatchlessGames.getChildren().add(game));
        matchingGames.stream()
                .filter(this::filterGame)
                .forEach(game -> vbMatchingGames.getChildren().add(game));
        treeGames.stream()
                .filter(this::filterGame)
                .forEach(game -> vbTreeGames.getChildren().add(game));
    }
    public boolean filterGame(GamePreview game) {
        GameMetadata metadata = game.getMetadata();

        boolean filterSuffix = numSuffixField.getText().isEmpty() || metadata.getNumSuffix() >= Integer.parseInt(numSuffixField.getText());
        Boolean filterPublicView = cbPublicViewFilter.isIndeterminate() ? null : cbPublicViewFilter.isSelected();
        Boolean filterSoloTeams = cbSoloTeamsFilter.isIndeterminate() ? null : cbSoloTeamsFilter.isSelected();
        Boolean filterOwnTeamsCreation = cbOwnTeamsCreationFilter.isIndeterminate() ? null : cbOwnTeamsCreationFilter.isSelected();
        boolean filterRole;
        switch (metadata.getAccountRole()) {
            case ADMIN -> filterRole = cbAdminFilter.isSelected();
            case PLAYER -> filterRole = cbPlayerFilter.isSelected();
            case SPECTATOR -> filterRole = cbViewerFilter.isSelected();
            default -> filterRole = false;
        }

        String searchQuery = gameSearchField.getText().toLowerCase();

        return metadata.getName().toLowerCase().startsWith(searchQuery)
                && filterSuffix
                && filterRole
                && (filterPublicView == null || filterPublicView == metadata.isPublicView()
                && (filterSoloTeams == null || filterSoloTeams == metadata.isSoloTeams())
                && (filterOwnTeamsCreation == null || filterOwnTeamsCreation == metadata.isAllowOwnTeamsCreation()));
    }

    public void setAccount(Account account) {
        this.currentAccount = account;

        loadGames();
        loadOutgoingRequests();
        loadIncomingRequests();
        loadInvitations();
    }

    @FXML
    public void onSearchButtonPressed() {
        bSearchGame.requestFocus();
        filterGames();
    }
    @FXML
    public void onReverseSortPressed() {
        sortAscending = !sortAscending;
        bReverseSort.setText(sortAscending ? "A-Z" : "Z-A");

        Collections.reverse(matchlessGames);
        Collections.reverse(matchingGames);
        Collections.reverse(treeGames);
        filterGames();
    }
    @FXML
    public void onReload() {
        vbOutgoingRequests.getChildren().clear();
        vbIncomingRequests.getChildren().clear();
        vbRequestAnswers.getChildren().clear();
        vbGameInvitations.getChildren().clear();
        loadOutgoingRequests();
        loadIncomingRequests();
        loadInvitations();

        loadGames();
    }

    private void setMetadata(GameMetadata metadata) {
        metadata.setAccount(currentAccount);
        gameMetadataById.put(metadata.getId(), metadata);
    }
    private void removeMetadata(GameMetadata metadata) {
        gameMetadataById.remove(metadata.getId());
    }
    private GameMetadata getMetadata(UUID gameId) {
        if (!gameMetadataById.containsKey(gameId)) {
            jdbi.useExtension(GameMetadataDao.class, dao -> {
                GameMetadata metadata = dao.getMetadataById(gameId, currentAccount.getName());
                metadata.setAccount(currentAccount);
                setMetadata(metadata);
            });
        }
        return gameMetadataById.get(gameId);
    }

    public void loadGames() {
        System.out.println("Spiele werden neu geladen...");
        GamePreview returnedPreview = null;

        jdbi.useExtension(GameMetadataDao.class, dao -> {
            // Lade die Metadata-Objekte der Spiele aus der Datenbank und speichere sie in den jeweiligen Listen als GamePreview-Objekte
            matchlessGames = dao.accessibleGames(currentAccount.getName(), GameType.MATCHLESS.getValue()).stream()
                    .map(this::setupGamePreview).collect(Collectors.toList());

            matchingGames = dao.accessibleGames(currentAccount.getName(), GameType.MATCHING.getValue()).stream()
                    .map(this::setupGamePreview).collect(Collectors.toList());

            treeGames = dao.accessibleGames(currentAccount.getName(), GameType.TREE.getValue()).stream()
                    .map(this::setupGamePreview).collect(Collectors.toList());
        });

        if (!sortAscending) {
            Collections.reverse(matchlessGames);
            Collections.reverse(matchingGames);
            Collections.reverse(treeGames);
        }

        filterGames();
        System.out.println("Spiele erfolgreich neu geladen.\n");
    }
    private GamePreview setupGamePreview(GameMetadata metadata) {
        metadata.setAccount(currentAccount);
        GamePreview preview = new GamePreview(metadata);
        preview.setOnDeleteGame(() -> onDeleteGame(preview));
        System.out.println("Spiel-Vorschau erfolgreich eingerichtet: " + metadata.getName());
        return preview;
    }
    private void onDeleteGame(GamePreview preview) {
        GameMetadata metadata = preview.getMetadata();
        switch (metadata.getType()) {
            case MATCHLESS -> {
                matchlessGames.remove(preview);
                vbMatchlessGames.getChildren().remove(preview);
            }
            case MATCHING -> {
                matchingGames.remove(preview);
                vbMatchingGames.getChildren().remove(preview);
            }
            case TREE -> {
                treeGames.remove(preview);
                vbTreeGames.getChildren().remove(preview);
            }
        }
        removeMetadata(metadata);
        System.out.println("Spiel erfolgreich gelöscht: " + metadata.getName());
    }

    public void loadOutgoingRequests() {
        jdbi.useExtension(RequestDao.class, dao -> {
            dao.getPendingOutgoingRequests(currentAccount.getName()).forEach(request -> {
                vbOutgoingRequests.getChildren().add(request);
                request.setMetadata(getMetadata(request.getGameId()));
            });
            dao.getAnsweredOutgoingRequests(currentAccount.getName()).forEach(request -> {
                vbRequestAnswers.getChildren().add(request);
                request.setMetadata(getMetadata(request.getGameId()));
            });
        });
    }
    public void loadIncomingRequests() {
        jdbi.useExtension(RequestDao.class, dao -> {
            dao.getIncomingRequests(currentAccount.getName()).forEach(request -> {
                vbIncomingRequests.getChildren().add(request);
            });
        });
    }
    public void loadInvitations() {
        jdbi.useExtension(InvitationDao.class, dao -> dao.getInvitations(currentAccount.getName()).forEach(invitation -> {
            vbGameInvitations.getChildren().add(invitation);
            invitation.setMetadata(getMetadata(invitation.getGameId()));
        }));
    }

    @FXML
    public void onLogout() {
        SceneLoader.openLoginScene((Stage) bLogout.getScene().getWindow());
    }
    @FXML
    public void onAccountSettings() {
        AccountSettingsDialog dialog = new AccountSettingsDialog();
        String oldName = currentAccount.getName();
        dialog.setAccount(currentAccount);
        dialog.initOwner(bAccountSettings.getScene().getWindow());
        dialog.showAndWait().ifPresent(account -> {
            jdbi.useHandle(handle -> handle.attach(AccountDao.class).updateAccount(oldName, currentAccount));
            Stage stage = (Stage) bAccountSettings.getScene().getWindow();
            stage.setTitle("Game Manager - Hauptmenü (Angemeldet als: " + currentAccount.getName() + ")");
        });
    }

    @FXML
    public void onCreateMatchlessGame() {
        createGame(GameType.MATCHLESS);
    }
    @FXML
    public void onCreateMatchingGame() {
        createGame(GameType.MATCHING);
    }
    @FXML
    public void onCreateTreeGame() {
        createGame(GameType.TREE);
    }
    public void createGame(GameType type) {
        GameCreationDialog dialog = new GameCreationDialog();
        dialog.setGameType(type);
        dialog.initOwner(bAccountSettings.getScene().getWindow());
        dialog.showAndWait().ifPresent(metadata -> {
            setMetadata(metadata);

            // Spiel wird dem Typ entsprechend in alle Tabellen der Hierarchie eingefügt (Siehe GameDao)
            jdbi.useHandle(handle -> handle.attach(GameDao.class).insertGame(metadata));
            jdbi.useHandle(handle -> handle.attach(WhitelistDao.class).setEntry(metadata.getId(), currentAccount.getName(), metadata.getWhitelistedRole()));

            // Lade die Spiele neu, um das neu erstellte Spiel anzuzeigen
            loadGames();

            // Öffne den Vorschau-Dialog des neuen Spiels
            GamePreview preview = findGamePreview(metadata);
            assert preview != null;
            preview.openDialog(gameSearchField.getScene().getWindow());
        });
    }
    private GamePreview findGamePreview(GameMetadata metadata) {
        // Gebe aus der entsprechenden Liste das erste GamePreview-Objekt mit der ID metadata.getId() zurück
        List<GamePreview> games = switch (metadata.getType()) {
            case MATCHLESS -> matchlessGames;
            case MATCHING -> matchingGames;
            case TREE -> treeGames;
        };
        for (GamePreview preview : games) {
            if (preview.getMetadata().getId().equals(metadata.getId())) {
                return preview;
            }
        }
        return null;
    }
}