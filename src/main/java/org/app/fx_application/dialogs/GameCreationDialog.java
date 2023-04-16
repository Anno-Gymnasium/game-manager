package org.app.fx_application.dialogs;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import org.app.GameRole;
import org.app.GameType;
import org.app.GameMetadata;

import org.app.fx_application.JdbiProvider;
import org.app.fx_application.daos.GameDao;
import org.jdbi.v3.core.Jdbi;

import java.util.UUID;

public class GameCreationDialog extends CustomDialog<GameMetadata> {
    @FXML private Label createGameLabel;
    @FXML private TextField gameNameField;
    @FXML private TextArea descriptionTextArea;
    @FXML private CheckBox cbSoloTeams, cbPublicView, cbOwnTeamsCreation;

    private GameMetadata metadata = null;
    private GameType gameType = null;
    private Jdbi jdbi = JdbiProvider.getInstance().getJdbi();

    public GameCreationDialog() {
        super();
        getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.APPLY);
        Button bCancel = (Button) getDialogPane().lookupButton(ButtonType.CANCEL);
        Button bConfirm = (Button) getDialogPane().lookupButton(ButtonType.APPLY);
        bCancel.setOnAction(actionEvent -> onCancel());
        bConfirm.addEventFilter(ActionEvent.ACTION, this::onConfirm);

        cbSoloTeams.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                cbOwnTeamsCreation.setDisable(true);
                cbOwnTeamsCreation.setSelected(false);
            } else {
                cbOwnTeamsCreation.setDisable(false);
            }
        });

        setResultConverter(buttonType -> {
            if (buttonType == ButtonType.APPLY) {
                return metadata;
            }
            return null;
        });
    }

    protected DialogPane loadDialogPane() {
        return loadDialogPane("game-creation-dialog.fxml");
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
        createGameLabel.setText(gameType.getName() + " erstellen");
    }

    private void onConfirm(ActionEvent actionEvent) {
        String gameName = gameNameField.getText().strip();
        int numSuffix = jdbi.withHandle(handle -> handle.attach(GameDao.class).countGamesWithName(gameName));
        String description = descriptionTextArea.getText().strip();
        metadata = new GameMetadata(UUID.randomUUID(), gameType.getValue(), gameName, numSuffix, description, GameRole.ADMIN.getValue(),
                GameRole.ADMIN.getValue(), cbPublicView.isSelected(), cbSoloTeams.isSelected(), cbOwnTeamsCreation.isSelected());
    }
}