package org.app.fx_application;

import javafx.fxml.FXML;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.app.GameRole;
import org.app.fx_application.dialogs.AccountSettingsDialog;
import org.app.fx_application.dialogs.GameCreationDialog;
import org.app.fx_application.selectables.PreviewGame;
import org.app.fx_application.selectables.SelectableOutgoingRequest;
import org.app.game_classes.*;

import java.util.Collections;
import java.util.List;

import org.jdbi.v3.core.Jdbi;

public class MainMenuController {
    @FXML private TextField gameSearchField, numSuffixField;
    @FXML private CheckBox cbPublicViewFilter, cbSoloTeamsFilter, cbOwnTeamsCreationFilter;
    @FXML private CheckBox cbAdminFilter, cbPlayerFilter, cbViewerFilter;
    @FXML private Button bReload, bAccountSettings, bLogout, bSearchGame, bReverseSort;
    @FXML private VBox vbMatchlessGames, vbMatchingGames, vbTreeGames;
    @FXML private VBox vbOutgoingRequests, vbIncomingRequests, vbRequestAnswers, vbGameInvitations;

    private Jdbi jdbi;
    private List<PreviewGame> matchlessGames, matchingGames, treeGames;
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

        jdbi = JdbiProvider.getInstance().getJdbi();
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
    public boolean filterGame(PreviewGame game) {
        boolean filterSuffix = numSuffixField.getText().isEmpty() || game.getGameNumSuffix() >= Integer.parseInt(numSuffixField.getText());
        Boolean filterPublicView = cbPublicViewFilter.isIndeterminate() ? null : cbPublicViewFilter.isSelected();
        Boolean filterSoloTeams = cbSoloTeamsFilter.isIndeterminate() ? null : cbSoloTeamsFilter.isSelected();
        Boolean filterOwnTeamsCreation = cbOwnTeamsCreationFilter.isIndeterminate() ? null : cbOwnTeamsCreationFilter.isSelected();
        boolean filterRole;
        switch (game.getAccountRole()) {
            case ADMIN -> filterRole = cbAdminFilter.isSelected();
            case PLAYER -> filterRole = cbPlayerFilter.isSelected();
            case SPECTATOR -> filterRole = cbViewerFilter.isSelected();
            default -> filterRole = false;
        }

        String searchQuery = gameSearchField.getText().toLowerCase();

        return game.getGameName().toLowerCase().startsWith(searchQuery)
                && filterSuffix
                && filterRole
                && (filterPublicView == null || filterPublicView == game.isPublicView())
                && (filterSoloTeams == null || filterSoloTeams == game.isSoloTeams())
                && (filterOwnTeamsCreation == null || filterOwnTeamsCreation == game.isAllowOwnTeamsCreation());
    }

    public void setAccount(Account account) {
        this.currentAccount = account;

        loadGames();
        loadOutgoingRequests();
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
        vbMatchlessGames.getChildren().clear();
        vbMatchingGames.getChildren().clear();
        vbTreeGames.getChildren().clear();
        loadGames();

        vbOutgoingRequests.getChildren().clear();
        vbIncomingRequests.getChildren().clear();
        vbRequestAnswers.getChildren().clear();
        vbGameInvitations.getChildren().clear();
        loadOutgoingRequests();
    }
    public void loadGames() {
        jdbi.useExtension(GameSelectionDao.class, dao -> {
            matchlessGames = dao.accessibleMatchlessGames(currentAccount.getName());
            matchingGames = dao.accessibleMatchingGames(currentAccount.getName());
            treeGames = dao.accessibleTreeGames(currentAccount.getName());
        });
        matchlessGames.forEach(game -> game.setAccount(currentAccount));
        matchingGames.forEach(game -> game.setAccount(currentAccount));
        treeGames.forEach(game -> game.setAccount(currentAccount));

        if (!sortAscending) {
            Collections.reverse(matchlessGames);
            Collections.reverse(matchingGames);
            Collections.reverse(treeGames);
        }

        filterGames();
    }
    public void loadOutgoingRequests() {
        jdbi.useHandle(handle -> {
            RequestDao dao = handle.attach(RequestDao.class);
            dao.getOutgoingRequests(currentAccount.getName()).forEach(request -> vbOutgoingRequests.getChildren().add(request));
        });
        vbOutgoingRequests.getChildren().add(new SelectableOutgoingRequest("abc", "def", 1, (byte) 2, "Test"));
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
        createGame(MatchlessGame.class);
    }
    @FXML
    public void onCreateMatchingGame() {
        createGame(MatchingGame.class);
    }
    @FXML
    public void onCreateTreeGame() {
        createGame(TreeGame.class);
    }
    public <G extends GenericGame<?, ?>> void createGame(Class<G> gameClass) {
        GameCreationDialog<G> dialog = new GameCreationDialog<>();
        dialog.setGameClass(gameClass);
        dialog.initOwner(bAccountSettings.getScene().getWindow());
        dialog.showAndWait().ifPresent(game -> {
            jdbi.useHandle(handle -> {
                GameDao dao = handle.attach(GameDao.class);
                int numSuffix = dao.countGamesWithName(game.getName());

                // Spiel wird dem Typ entsprechend in alle Tabellen der Hierarchie eingefügt
                dao.insertAsGenericGame(game, numSuffix);
                if (gameClass == MatchlessGame.class) {
                    dao.insertAsMatchlessGame(game);
                    return;
                }
                dao.insertAsGenericMatchingGame(game);
                if (gameClass == MatchingGame.class) {
                    dao.insertAsMatchingGame(game);
                    return;
                }
                dao.insertAsTreeGame(game);
            });
            jdbi.useHandle(handle -> {
                RolesDao dao = handle.attach(RolesDao.class);
                dao.setWhitelistRole(game.getId(), currentAccount.getName(), GameRole.ADMIN.getValue());
            });

            // Lade die Spiele neu, um das neu erstellte Spiel anzuzeigen
            onReload();

            // Finde das neu erstellte Spiel und öffne seinen Preview-Dialog
            List<PreviewGame> gameList;
            if (gameClass == org.app.game_classes.MatchlessGame.class) {
                gameList = matchlessGames;
            } else if (gameClass == org.app.game_classes.MatchingGame.class) {
                gameList = matchingGames;
            } else if (gameClass == org.app.game_classes.TreeGame.class) {
                gameList = treeGames;
            } else {
                throw new IllegalArgumentException("Ungültige Spielklasse");
            }
            gameList.stream()
                    .filter(pGame -> pGame.getGameId().equals(game.getId()))
                    .findFirst()
                    .ifPresent(PreviewGame::openDialog);
        });
    }
}