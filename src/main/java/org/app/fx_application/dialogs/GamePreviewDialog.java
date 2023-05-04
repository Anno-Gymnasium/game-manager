package org.app.fx_application.dialogs;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import org.app.GameRole;
import org.app.GameMetadata;
import org.app.fx_application.SceneLoader;
import org.app.fx_application.controllers.MainMenuController;
import org.app.fx_application.daos.GameDao;
import org.app.fx_application.daos.GameMetadataDao;
import org.app.fx_application.daos.RequestDao;

import org.app.fx_application.JdbiProvider;
import org.app.game_classes.GenericGame;
import org.jdbi.v3.core.Jdbi;

public class GamePreviewDialog extends CustomDialog<GameEditDialogResult> {
    @FXML private Label gameTypeLabel, adminOnlineLabel;
    @FXML private TextField gameNameField;
    @FXML private TextArea descriptionTextArea;
    @FXML private CheckBox cbSoloTeams, cbPublicView, cbOwnTeamsCreation;
    @FXML private ComboBox<GameRole> cboxJoinRole;
    @FXML private Hyperlink hlCreateRequest, hlInvite, hlEditWhitelist;
    @FXML private Button deleteButton;
    private Button saveButton;

    private GameMetadata metadata;
    private boolean gameChanged = false;
    private boolean gameSaved = true;
    private boolean adminOnline = false;
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

        gameNameField.setEditable(false);
        descriptionTextArea.setEditable(false);
        cbSoloTeams.setDisable(true);
        cbPublicView.setDisable(true);
        cbOwnTeamsCreation.setDisable(true);

        deleteButton.setVisible(false);
        hlInvite.setVisible(false);
        hlEditWhitelist.setVisible(false);

        cboxJoinRole.setConverter(GameRole.STRING_CONVERTER);

        setResultConverter(buttonType -> new GameEditDialogResult(gameChanged, false));
    }

    protected DialogPane loadDialogPane() {
        return loadDialogPane("game-preview-dialog.fxml");
    }

    public void setMetadata(GameMetadata metadata) {
        this.metadata = metadata;
        gameTypeLabel.setText("Typ: " + metadata.getType().getName());
        gameNameField.setText(metadata.getName());
        descriptionTextArea.setText(metadata.getDescription());
        cbSoloTeams.setSelected(metadata.isSoloTeams());
        cbPublicView.setSelected(metadata.isPublicView());
        cbOwnTeamsCreation.setSelected(metadata.isAllowOwnTeamsCreation());

        GameRole accountRole = metadata.getAccountRole();
        switch (accountRole) {
            case SPECTATOR -> cboxJoinRole.getItems().add(GameRole.SPECTATOR);
            case PLAYER -> cboxJoinRole.getItems().addAll(GameRole.PLAYER, GameRole.SPECTATOR);
            case ADMIN -> {
                updateAdminOnline();
                if (!adminOnline) cboxJoinRole.getItems().add(GameRole.ADMIN);

                cboxJoinRole.getItems().addAll(GameRole.PLAYER, GameRole.SPECTATOR);
                getDialogPane().getButtonTypes().add(ButtonType.APPLY);
                saveButton = (Button) getDialogPane().lookupButton(ButtonType.APPLY);
                saveButton.setText("Änderungen speichern");
                saveButton.addEventFilter(ActionEvent.ACTION, this::onSaveGame);
                saveButton.setDisable(true);

                gameNameField.setEditable(true);
                descriptionTextArea.setEditable(true);
                cbPublicView.setDisable(false);
                if (!metadata.isSoloTeams()) cbOwnTeamsCreation.setDisable(false);
                hlCreateRequest.setDisable(true);

                deleteButton.setVisible(true);
                hlInvite.setVisible(true);
                hlEditWhitelist.setVisible(true);

                gameNameField.textProperty().addListener((observableValue, vOld, vNew) -> {
                    if (vNew.length() > GenericGame.MAX_NAME_LENGTH) {
                        gameNameField.setText(vOld);
                        return;
                    }
                    onGameChanged();
                });
                descriptionTextArea.textProperty().addListener((observableValue, vOld, vNew) -> {
                    if (vNew.length() > GenericGame.MAX_DESCRIPTION_LENGTH) {
                        descriptionTextArea.setText(vOld);
                        return;
                    }
                    onGameChanged();
                });
            }
        }
        cboxJoinRole.getSelectionModel().selectFirst();
    }

    private void updateAdminOnline() {
        adminOnline = jdbi.withExtension(GameDao.class, dao -> dao.isAdminOnline(metadata.getId(), GenericGame.ADMIN_PING_TIMEOUT_SECONDS));
        adminOnlineLabel.setVisible(adminOnline);
    }

    @FXML
    public void onNameFieldEnter() {
        descriptionTextArea.requestFocus();
    }
    @FXML
    public void onGameChanged() {
        gameChanged = true;
        if (gameNameField.getText().strip().equals(metadata.getName()) &&
                descriptionTextArea.getText().strip().equals(metadata.getDescription()) &&
                cbPublicView.isSelected() == metadata.isPublicView() &&
                cbOwnTeamsCreation.isSelected() == metadata.isAllowOwnTeamsCreation()) {
            gameSaved = true;
            saveButton.setDisable(true);
            return;
        }

        assert saveButton != null;
        gameSaved = false;
        saveButton.setDisable(false);
    }
    public void onSaveGame(ActionEvent actionEvent) {
        actionEvent.consume();
        if (gameSaved) return;

        if (gameNameField.getText().isEmpty()) {
            gameNameField.requestFocus();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Fehler");
            alert.setHeaderText("Kein Name angegeben");
            alert.setContentText("Bitte geben Sie einen Namen für das Spiel an.");
            alert.showAndWait();
        }

        metadata.setName(gameNameField.getText().strip());
        metadata.setDescription(descriptionTextArea.getText().strip());
        metadata.setPublicView(cbPublicView.isSelected());
        metadata.setAllowOwnTeamsCreation(cbOwnTeamsCreation.isSelected());

        saveButton.setDisable(true);
        jdbi.useHandle(handle -> {
            GameDao dao = handle.attach(GameDao.class);

            boolean nameChanged = !gameNameField.getText().strip().equals(metadata.getName());
            if (nameChanged) {
                int numSuffix = dao.countGamesWithName(metadata.getName());
                metadata.setNumSuffix(numSuffix);
            }
            handle.attach(GameMetadataDao.class).updateGameMetadata(metadata);
        });
        gameSaved = true;
    }
    private void onJoinGame(ActionEvent actionEvent) {
        if (cboxJoinRole.getSelectionModel().getSelectedItem() == null) {
            actionEvent.consume();
            return;
        }
        joinGame();
    }
    public void joinGame() {
        GameRole joinRole = cboxJoinRole.getSelectionModel().getSelectedItem();
        if (joinRole == null) return;

        updateAdminOnline();
        if (adminOnline && joinRole == GameRole.ADMIN) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Fehler");
            alert.setHeaderText("Admin bereits online");
            alert.setContentText("Es ist bereits ein Admin online. Sie können nicht als Admin beitreten.");
            alert.showAndWait();
            return;
        }

        Stage stage = (Stage) getOwner().getScene().getWindow();
        SceneLoader.openGameScene(stage, metadata, joinRole);
    }

    @FXML
    private void onCreateRequest() {
        boolean existsRequest = jdbi.withHandle(handle -> handle.attach(RequestDao.class).existsRequest(metadata.getAccount().getName(), metadata.getId()));
        if (existsRequest) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Keine Anfrage möglich");
            alert.setHeaderText("Anfrage bereits vorhanden");
            alert.setContentText("Sie haben bereits eine Anfrage für dieses Spiel gestellt.");
            alert.showAndWait();
            return;
        }
        RoleRequestDialog dialog = new RoleRequestDialog();
        dialog.setMetadata(metadata);
        dialog.showAndWait().ifPresent(request -> {
            jdbi.useHandle(handle -> handle.attach(RequestDao.class).insertRequest(request));
            hlCreateRequest.setDisable(true);
            Event.fireEvent(getOwner(), new Event(MainMenuController.RELOAD));
        });
    }
    @FXML
    private void onInvite() {
        InviteDialog dialog = new InviteDialog();
        dialog.setMetadata(metadata);
        dialog.showAndWait();
    }
    @FXML
    private void onEditWhitelist() {
        WhitelistDialog dialog = new WhitelistDialog();
        dialog.setMetadata(metadata);
        dialog.showAndWait();
    }

    @FXML
    private void onDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Spiel löschen");
        alert.setHeaderText("Spiel löschen");
        alert.setContentText("Sind Sie sicher, dass Sie das Spiel löschen möchten?");

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                deleteGame();
            }
        });
    }
    private void deleteGame() {
        jdbi.useHandle(handle -> {
            GameDao dao = handle.attach(GameDao.class);
            dao.deleteGame(metadata.getId());
        });
        setResult(new GameEditDialogResult(gameChanged, true));
        close();
    }
}