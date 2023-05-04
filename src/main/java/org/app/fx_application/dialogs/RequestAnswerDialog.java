package org.app.fx_application.dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import org.app.GameRole;
import org.app.GameMetadata;
import org.app.fx_application.daos.AccountDao;
import org.app.game_classes.Account;
import org.app.fx_application.selectables.SelectableIncomingRequest;

import org.app.fx_application.JdbiProvider;
import org.jdbi.v3.core.Jdbi;

public class RequestAnswerDialog extends CustomDialog<GameRole> {
    @FXML private Label requestGameNameLabel, currentRoleLabel, requestedRoleLabel;
    @FXML private Hyperlink hlAccountName;
    @FXML private TextArea messageArea;
    @FXML private ComboBox<GameRole> cboxAcceptedRole;

    @FXML private SelectableIncomingRequest request;
    private Jdbi jdbi = JdbiProvider.getInstance().getJdbi();

    public RequestAnswerDialog() {
        super();
        Button bConfirm = (Button) getDialogPane().lookupButton(ButtonType.OK);
        bConfirm.setText("Senden");

        setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return cboxAcceptedRole.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        cboxAcceptedRole.setConverter(GameRole.STRING_CONVERTER);
    }

    protected DialogPane loadDialogPane() {
        return loadDialogPane("request-answer-dialog.fxml");
    }

    public void setRequest(SelectableIncomingRequest request) {
        this.request = request;
        requestGameNameLabel.setText("Anfrage für " + GameMetadata.getCompositeName(request.getGameName(), request.getGameNumSuffix()));
        messageArea.setText(request.getMessage());
        hlAccountName.setText(request.getAccountName());
        currentRoleLabel.setText(request.getCurrentRole().getName());
        requestedRoleLabel.setText(request.getRequestedRole().getName());

        // Füge alle Rollen zur ComboBox hinzu, die höher sind als die aktuelle Rolle des anfragenden Accounts, beginnend mit der höchsten Rolle (Admin)
        byte currentRoleValue = request.getCurrentRole().getValue();
        for (byte i = (byte) 3; i > currentRoleValue; i--) {
            cboxAcceptedRole.getItems().add(GameRole.getRole(i));
        }
        cboxAcceptedRole.getSelectionModel().select(request.getRequestedRole());
    }
    @FXML
    public void openAccountInfo() {
        Account account = jdbi.withHandle(handle -> handle.attach(AccountDao.class).getByName(request.getAccountName()));
        CustomDialog.openAccountInfoAlert(account);
    }
}