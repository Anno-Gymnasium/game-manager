package org.app.fx_application.dialogs;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import org.app.GameRole;
import org.app.fx_application.selectables.PreviewGame;
import org.app.fx_application.selectables.SelectableOutgoingRequest;
import org.app.game_classes.Account;

import org.app.fx_application.JdbiProvider;
import org.jdbi.v3.core.Jdbi;

public class RoleRequestDialog extends CustomDialog<SelectableOutgoingRequest> {
    @FXML private Label requestRoleLabel;
    @FXML private ComboBox<GameRole> cboxRole;
    @FXML private TextArea messageArea;

    private Account currentAccount;
    private PreviewGame previewGame;
    private Jdbi jdbi = JdbiProvider.getInstance().getJdbi();
    private SelectableOutgoingRequest request;

    public RoleRequestDialog() {
        super();
        Button bCancel = (Button) getDialogPane().lookupButton(ButtonType.CANCEL);
        Button bConfirm = (Button) getDialogPane().lookupButton(ButtonType.OK);
        bConfirm.setText("Senden");
        bConfirm.addEventFilter(ActionEvent.ACTION, this::onConfirm);

        cboxRole.setConverter(new StringConverter<>() {
            @Override public String toString(GameRole gameRole) {
                return gameRole.getName();
            }
            @Override public GameRole fromString(String s) {
                return null;
            }
        });

        setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return request;
            }
            return null;
        });
    }

    protected DialogPane loadDialogPane() {
        return loadDialogPane("role-request-dialog.fxml");
    }

    public void setAccount(Account account) {
        currentAccount = account;
    }
    public void setPreviewGame(PreviewGame previewGame) {
        this.previewGame = previewGame;

        // Füge alle Rollen zur Combobox hinzu, die höher sind als die aktuelle Rolle des Accounts
        byte currentRoleValue = previewGame.getWhitelistedRole();
        for (byte i = (byte) (currentRoleValue + 1); i < GameRole.values().length; i++) {
            cboxRole.getItems().add(GameRole.values()[i]);
        }
        cboxRole.getSelectionModel().selectFirst();
    }

    private void onConfirm(ActionEvent actionEvent) {
        request = new SelectableOutgoingRequest(currentAccount.getName(), previewGame.getGameName(),
                previewGame.getGameNumSuffix(), cboxRole.getValue().getValue(), messageArea.getText());
    }
}