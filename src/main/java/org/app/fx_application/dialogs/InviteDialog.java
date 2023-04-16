package org.app.fx_application.dialogs;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import javafx.util.StringConverter;
import org.app.GameMetadata;
import org.app.GameRole;
import org.app.game_classes.Account;
import org.app.fx_application.daos.WhitelistDao;
import org.app.fx_application.daos.AccountDao;
import org.app.fx_application.daos.InvitationDao;

import org.app.fx_application.JdbiProvider;
import org.jdbi.v3.core.Jdbi;

public class InviteDialog extends CustomDialog<ButtonType> {
    public static final int MAX_MESSAGE_LENGTH = 70;
    private GameMetadata metadata;
    private Account invitedAccount;
    private boolean searchQueryChanged = false;
    private Jdbi jdbi = JdbiProvider.getInstance().getJdbi();

    private Label selectedAccountLabel = null;
    @FXML private Label gameNameLabel;
    private final Label messageExceedLabel;
    @FXML private TextField searchAccountField;
    @FXML private Button searchAccountButton, bShowAccountInfo, bSend;
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
        bShowAccountInfo.setDisable(true);

        getDialogPane().lookupButton(ButtonType.CLOSE).getStyleClass().add("no-skin-button");

        // Listener zur Navigation durch die Liste der Accounts mit den Pfeiltasten
        vbAccounts.setOnKeyPressed(keyEvent -> {
            switch (keyEvent.getCode()) {
                case UP -> selectPreviousAccount();
                case DOWN -> selectNextAccount();
            }
        });

        searchAccountField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchQueryChanged = true;
        });
        searchAccountField.requestFocus();

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

        cboxRole.setConverter(new StringConverter<>() {
            @Override public String toString(GameRole gameRole) {
                return gameRole == null ? null : gameRole.getName();
            }
            @Override public GameRole fromString(String s) {return null;}
        });
    }

    protected DialogPane loadDialogPane() {
        return loadDialogPane("invite-dialog.fxml");
    }

    public void setMetadata(GameMetadata metadata) {
        this.metadata = metadata;
        gameNameLabel.setText("Einladungen für " + metadata.getCompositeName() + " versenden");
    }
    @FXML
    private void onSearchFieldEnter() {
        searchAccountButton.requestFocus();
        searchAccountButton.fire();
    }
    @FXML
    private void onSearchAccounts() {
        if (!searchQueryChanged) return;
        searchQueryChanged = false;

        bSend.setDisable(true);
        selectedAccountLabel = null;
        vbAccounts.getChildren().clear();
        invitedAccount = null;
        bShowAccountInfo.setDisable(true);
        cboxRole.getItems().clear();
        String query = searchAccountField.getText().strip();
        if (query.isEmpty()) {
            return;
        }
        jdbi.withHandle(handle -> handle.attach(InvitationDao.class).getInvitableAccountNamesByQuery(metadata.getId(), metadata.getAccount().getName(), query))
                .forEach(accountName -> {
                    Label accountLabel = new Label(accountName);
                    accountLabel.setFont(javafx.scene.text.Font.font(13));
                    accountLabel.setMinWidth(295);
                    accountLabel.getStyleClass().add("clickable-pane");
                    accountLabel.setOnMouseClicked(event -> {
                        selectedAccountLabel = accountLabel;
                        accountLabel.setStyle("-fx-background-color: #3cb8d4");
                        updateAvailableRoles(accountName);
                        if (cboxRole.getItems().isEmpty()) {
                            return;
                        }
                        invitedAccount = jdbi.withHandle(handle -> handle.attach(AccountDao.class).getByName(accountName));
                        bShowAccountInfo.setDisable(false);
                    });
                    if (vbAccounts.getChildren().isEmpty()) triggerMouseClick(accountLabel);
                    vbAccounts.getChildren().add(accountLabel);
                });
    }
    private void updateAvailableRoles(String accountName) {
        cboxRole.getItems().clear();
        byte currentWlRoleValue = jdbi.withHandle(handle -> handle.attach(WhitelistDao.class).getWhitelistRole(metadata.getId(), accountName));
        for (byte i = (byte) (currentWlRoleValue + 1); i <= 3; i++) {
            cboxRole.getItems().add(GameRole.values()[i]);
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
        jdbi.useHandle(handle -> handle.attach(WhitelistDao.class).setWhitelistRole(metadata.getId(),
                invitedAccount.getName(), cboxRole.getValue().getValue()));
        jdbi.useHandle(handle -> handle.attach(InvitationDao.class).setInvitation(metadata.getId(),
                invitedAccount.getName(), messageArea.getText().strip()));
    }
    @FXML
    private void onShowAccountInfo() {
        if (invitedAccount == null) {
            return;
        }
        CustomDialog.openAccountInfoAlert(invitedAccount);
    }

    private void selectNextAccount() {
        if (vbAccounts.getChildren().isEmpty() || selectedAccountLabel == null) {
            return;
        }
        int index = vbAccounts.getChildren().indexOf(selectedAccountLabel);
        if (index == vbAccounts.getChildren().size() - 1) {
            return;
        }
        selectedAccountLabel.setStyle(null);
        selectedAccountLabel = (Label) vbAccounts.getChildren().get(index + 1);

        triggerMouseClick(selectedAccountLabel);
    }
    private void selectPreviousAccount() {
        if (vbAccounts.getChildren().isEmpty() || selectedAccountLabel == null) {
            return;
        }
        int index = vbAccounts.getChildren().indexOf(selectedAccountLabel);
        if (index == 0) {
            return;
        }
        selectedAccountLabel.setStyle(null);
        selectedAccountLabel = (Label) vbAccounts.getChildren().get(index - 1);

        triggerMouseClick(selectedAccountLabel);
    }
    private void triggerMouseClick(Label label) {
        label.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY,
                1, true, true, true, true, true, true, true, true, true, true, null));
    }
}