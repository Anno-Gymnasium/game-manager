package org.app.fx_application.dialogs;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import javafx.util.StringConverter;
import org.app.GameMetadata;
import org.app.GameRole;
import org.app.game_classes.Account;
import org.app.fx_application.daos.WhitelistDao;
import org.app.fx_application.daos.AccountDao;
import org.app.fx_application.daos.InvitationDao;

import java.util.List;

public class InviteDialog extends AccountSearchingDialog<ButtonType> {
    public static final int MAX_MESSAGE_LENGTH = 70;
    private GameMetadata metadata;
    private Account invitedAccount;

    @FXML private Label gameNameLabel;
    private final Label messageExceedLabel;
    private Button bSend;
    @FXML private TextArea messageArea;
    @FXML private VBox vbAccounts, vbMain;
    @FXML private ComboBox<GameRole> cboxRole;

    public InviteDialog() {
        super();
        bSend = (Button) getDialogPane().lookupButton(ButtonType.OK);
        bSend.getStyleClass().add("no-skin-button");
        bSend.setText("Senden");
        bSend.addEventFilter(ActionEvent.ACTION, this::onSend);
        bSend.setDisable(true);

        getDialogPane().lookupButton(ButtonType.CLOSE).getStyleClass().add("no-skin-button");

        messageExceedLabel = new Label("Maximale Zeichenanzahl von " + MAX_MESSAGE_LENGTH + " erreicht");
        messageExceedLabel.setStyle("-fx-text-fill: red");
        VBox.setVgrow(messageExceedLabel, javafx.scene.layout.Priority.ALWAYS);

        messageArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > MAX_MESSAGE_LENGTH) {
                messageArea.setText(oldValue);
                if (vbMain.getChildren().get(vbMain.getChildren().size() - 1) != messageExceedLabel) {
                    vbMain.getChildren().add(messageExceedLabel);
                }
            }
            else {
                vbMain.getChildren().remove(messageExceedLabel);
            }
        });

        cboxRole.setConverter(GameRole.STRING_CONVERTER);
    }
    protected DialogPane loadDialogPane() {
        return loadDialogPane("invite-dialog.fxml");
    }
    public void setMetadata(GameMetadata metadata) {
        this.metadata = metadata;
        gameNameLabel.setText("Einladungen für " + metadata.getCompositeName() + " versenden");
    }

    private void updateAvailableRoles(String accountName) {
        cboxRole.getItems().clear();
        byte currentWlRoleValue = jdbi.withHandle(handle -> handle.attach(WhitelistDao.class).getWhitelistRole(metadata.getId(), accountName));
        for (byte i = (byte) (currentWlRoleValue + 1); i <= 3; i++) {
            cboxRole.getItems().add(GameRole.getRole(i));
        }
        if (cboxRole.getItems().isEmpty()) {
            cboxRole.setPromptText("Account ist bereits Admin");
            bSend.setDisable(true);
            return;
        }
        cboxRole.getSelectionModel().selectFirst();
        bSend.setDisable(false);
    }
    private void onSend(ActionEvent actionEvent) {
        actionEvent.consume();
        if (invitedAccount == null) {
            return;
        }
        bSend.setDisable(true);
        vbAccounts.getChildren().remove(selectedAccountLabel);
        jdbi.useHandle(handle -> handle.attach(WhitelistDao.class).setEntry(metadata.getId(),
                invitedAccount.getName(), cboxRole.getValue().getValue()));
        jdbi.useHandle(handle -> handle.attach(InvitationDao.class).setInvitation(metadata.getId(),
                invitedAccount.getName(), messageArea.getText().strip()));
    }
    @FXML
    protected void onShowAccountInfo() {
        if (invitedAccount == null) return;
        CustomDialog.openAccountInfoAlert(invitedAccount);
    }

    @Override
    protected void beforeSearchAccounts() {
        bSend.setDisable(true);
        invitedAccount = null;
        cboxRole.getItems().clear();
    }
    @Override
    protected List<String> getQueriedAccountNames(String query) {
        return jdbi.withHandle(handle -> handle.attach(InvitationDao.class).getInvitableAccountNamesByQuery(metadata.getId(), metadata.getAccount().getName(), query));
    }
    @Override
    protected void onLabelClick(String accountName) {
        updateAvailableRoles(accountName);
        if (cboxRole.getItems().isEmpty()) {
            return;
        }
        invitedAccount = loadSelectedAccount();
    }
}