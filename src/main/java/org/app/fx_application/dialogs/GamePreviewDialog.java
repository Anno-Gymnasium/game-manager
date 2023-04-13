package org.app.fx_application.dialogs;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import org.app.GameRole;
import org.app.fx_application.GameDao;
import org.app.fx_application.RequestDao;
import org.app.fx_application.selectables.PreviewGame;
import org.app.game_classes.Account;
import org.app.game_classes.WhiteListEntry;

import org.app.fx_application.JdbiProvider;
import org.jdbi.v3.core.Jdbi;

public class GamePreviewDialog extends CustomDialog<WhiteListEntry> {
    @FXML private Label gameTypeLabel, adminOnlineLabel;
    @FXML private TextField gameNameField;
    @FXML private TextArea descriptionTextArea;
    @FXML private CheckBox cbSoloTeams, cbPublicView, cbOwnTeamsCreation;
    @FXML private ComboBox<GameRole> cboxJoinRole;
    @FXML private Hyperlink hlCreateRequest, hlInvite, hlEditWhitelist;
    private Button saveButton;

    private Account currentAccount;
    private PreviewGame previewGame;
    private WhiteListEntry whiteListEntry;
    private boolean gameSaved = true;
    private Jdbi jdbi = JdbiProvider.getInstance().getJdbi();

    public GamePreviewDialog() {
        super();
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button bCancel = (Button) getDialogPane().lookupButton(ButtonType.CANCEL);
        bCancel.setText("Schließen");
        Button bConfirm = (Button) getDialogPane().lookupButton(ButtonType.OK);
        bConfirm.setText("Beitreten");
        bCancel.setOnAction(actionEvent -> onCancel());
        bConfirm.addEventFilter(ActionEvent.ACTION, this::onJoinGame);

        gameNameField.setDisable(true);
        descriptionTextArea.setDisable(true);
        cbSoloTeams.setDisable(true);
        cbPublicView.setDisable(true);
        cbOwnTeamsCreation.setDisable(true);

        hlInvite.setVisible(false);
        hlEditWhitelist.setVisible(false);

        cboxJoinRole.setConverter(new StringConverter<>() {
            @Override public String toString(GameRole gameRole) {
                return gameRole.getName();
            }
            @Override public GameRole fromString(String s) {
                return null;
            }
        });

        setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return whiteListEntry;
            }
            return null;
        });
    }

    protected DialogPane loadDialogPane() {
        return loadDialogPane("game-preview-dialog.fxml");
    }

    public void setAccount(Account currentAccount) {
        this.currentAccount = currentAccount;
    }
    public void setPreviewGame(PreviewGame previewGame) {
        this.previewGame = previewGame;
        gameTypeLabel.setText("Typ: " + previewGame.getGameType());
        gameNameField.setText(previewGame.getGameName());
        descriptionTextArea.setText(previewGame.getGameDescription());
        cbSoloTeams.setSelected(previewGame.isSoloTeams());
        cbPublicView.setSelected(previewGame.isPublicView());
        cbOwnTeamsCreation.setSelected(previewGame.isAllowOwnTeamsCreation());

        GameRole accountRole = previewGame.getAccountRole();
        switch (accountRole) {
            case SPECTATOR -> cboxJoinRole.getItems().add(GameRole.SPECTATOR);
            case PLAYER -> cboxJoinRole.getItems().addAll(GameRole.PLAYER, GameRole.SPECTATOR);
            case ADMIN -> {
                boolean adminOnline = jdbi.withHandle(handle -> handle.attach(GameDao.class).isAdminOnline(previewGame.getGameId()));
                if (adminOnline) adminOnlineLabel.setVisible(true);
                else cboxJoinRole.getItems().add(GameRole.ADMIN);

                cboxJoinRole.getItems().addAll(GameRole.PLAYER, GameRole.SPECTATOR);
                getDialogPane().getButtonTypes().add(ButtonType.APPLY);
                saveButton = (Button) getDialogPane().lookupButton(ButtonType.APPLY);
                saveButton.setText("Änderungen speichern");
                saveButton.addEventFilter(ActionEvent.ACTION, this::onSaveGame);
                saveButton.setDisable(true);

                gameNameField.setDisable(false);
                descriptionTextArea.setDisable(false);
                cbPublicView.setDisable(false);
                cbOwnTeamsCreation.setDisable(false);
                hlCreateRequest.setDisable(true);

                hlInvite.setVisible(true);
                hlEditWhitelist.setVisible(true);

                gameNameField.textProperty().addListener((observableValue, vOld, vNew) -> onGameChanged());
                descriptionTextArea.textProperty().addListener((observableValue, vOld, vNew) -> onGameChanged());
            }
        }
        cboxJoinRole.getSelectionModel().selectFirst();
    }

    @FXML
    public void onNameFieldEnter() {
        descriptionTextArea.requestFocus();
    }
    @FXML
    public void onGameChanged() {
        if (gameNameField.getText().strip().equals(previewGame.getGameName()) &&
                descriptionTextArea.getText().strip().equals(previewGame.getGameDescription()) &&
                cbPublicView.isSelected() == previewGame.isPublicView() &&
                cbOwnTeamsCreation.isSelected() == previewGame.isAllowOwnTeamsCreation()) {
            gameSaved = true;
            saveButton.setDisable(true);
            return;
        }

        assert saveButton != null;
        gameSaved = false;
        saveButton.setDisable(false);
    }
    public boolean onSaveGame(ActionEvent actionEvent) {
        actionEvent.consume();
        if (gameNameField.getText().isEmpty()) {
            gameNameField.requestFocus();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Fehler");
            alert.setHeaderText("Kein Name angegeben");
            alert.setContentText("Bitte geben Sie einen Namen für das Spiel an.");
            alert.showAndWait();
            return false;
        }

        boolean nameChanged = !gameNameField.getText().strip().equals(previewGame.getGameName());
        previewGame.setGameName(gameNameField.getText().strip());
        previewGame.setGameDescription(descriptionTextArea.getText().strip());
        previewGame.setPublicView(cbPublicView.isSelected());
        previewGame.setOwnTeamsCreation(cbOwnTeamsCreation.isSelected());

        saveButton.setDisable(true);
        jdbi.useHandle(handle -> {
            GameDao dao = handle.attach(GameDao.class);
            if (nameChanged) {
                int numSuffix = dao.countGamesWithName(previewGame.getGameName());
                previewGame.setGameNumSuffix(numSuffix);
            }
            dao.updateGamePreview(previewGame);
        });
        return gameSaved = true;
    }
    public void onJoinGame(ActionEvent actionEvent) {
        if (cboxJoinRole.getSelectionModel().getSelectedItem() == null) {
            actionEvent.consume();
            return;
        }
        whiteListEntry = new WhiteListEntry(currentAccount.getName(), previewGame.getGameId(), cboxJoinRole.getSelectionModel().getSelectedItem().getValue());
    }

    @FXML
    public void onCreateRequest() {
        boolean existsRequest = jdbi.withHandle(handle -> handle.attach(RequestDao.class).existsRequest(currentAccount.getName(), previewGame.getGameId()));
        if (existsRequest) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Keine Anfrage möglich");
            alert.setHeaderText("Anfrage bereits vorhanden");
            alert.setContentText("Sie haben bereits eine Anfrage für dieses Spiel gestellt.");
            alert.showAndWait();
            return;
        }
        RoleRequestDialog dialog = new RoleRequestDialog();
        dialog.setAccount(currentAccount);
        dialog.setPreviewGame(previewGame);
        dialog.showAndWait().ifPresent(request -> {
            jdbi.useHandle(handle -> handle.attach(RequestDao.class).insertRequest(request));
            hlCreateRequest.setDisable(true);
        });
    }
    @FXML
    public void onInvite() {

    }
    @FXML
    public void onEditWhitelist() {

    }
}