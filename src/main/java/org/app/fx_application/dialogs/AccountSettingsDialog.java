package org.app.fx_application.dialogs;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javafx.stage.Stage;
import org.app.fx_application.daos.AccountDao;
import org.app.fx_application.JdbiProvider;
import org.app.fx_application.SceneLoader;
import org.app.game_classes.Account;
import org.jdbi.v3.core.Jdbi;

import java.time.format.DateTimeFormatter;

public class AccountSettingsDialog extends CustomDialog<Account> {
    @FXML private Label accountNameLabel, descriptionExceededLabel, dateCreatedLabel;
    @FXML private TextArea descriptionTextArea;
    @FXML private CheckBox cbPassiveGameJoining;
    @FXML private TextField oldPasswordField, newPasswordField, renameField;
    @FXML private Button bDeleteAccount;
    private Account account;
    private Jdbi jdbi;

    public AccountSettingsDialog() {
        super();
        getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.APPLY);
        Button bCancel = (Button) getDialogPane().lookupButton(ButtonType.CANCEL);
        Button bConfirm = (Button) getDialogPane().lookupButton(ButtonType.APPLY);
        bCancel.setOnAction(actionEvent -> onCancel());
        bConfirm.addEventFilter(ActionEvent.ACTION, this::onConfirm);

        descriptionExceededLabel.setText("Maximale Zeichenanzahl von " + Account.MAX_DESCRIPTION_LENGTH + " erreicht");

        descriptionTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > Account.MAX_DESCRIPTION_LENGTH) {
                descriptionTextArea.setText(oldValue);
                descriptionExceededLabel.setVisible(true);
            }
            else {
                descriptionExceededLabel.setVisible(false);
            }
        });

        oldPasswordField.setOnAction(actionEvent -> newPasswordField.requestFocus());

        setResultConverter(buttonType -> {
            if (buttonType == ButtonType.APPLY) {
                return account;
            }
            return null;
        });

        jdbi = JdbiProvider.getInstance().getJdbi();
    }

    protected DialogPane loadDialogPane() {
        return loadDialogPane("account-settings-dialog.fxml");
    }
    public void setAccount(Account account) {
        this.account = account;

        accountNameLabel.setText(account.getName());
        dateCreatedLabel.setText("Erstellt am: " + account.getDateCreated().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        descriptionTextArea.setText(account.getDescription());
        cbPassiveGameJoining.setSelected(account.isAllowPassiveGameJoining());
    }

    @FXML
    private void onDeleteAccount() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Account löschen");
        alert.setHeaderText("Diesen Account löschen");
        alert.setContentText("Möchten Sie den Account wirklich löschen?");

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                jdbi.useHandle(handle -> handle.attach(AccountDao.class).deleteAccount(account.getName()));
                SceneLoader.openLoginScene((Stage) getOwner());
                onCancel();
            }
        });
    }

    private void onConfirm(ActionEvent actionEvent) {
        String newName = renameField.getText().strip();
        if (!newName.isEmpty() && !newName.equals(account.getName())) {
            boolean existsWithName = jdbi.withHandle(handle -> handle.attach(AccountDao.class).existsByName(newName));
            if (existsWithName) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Umbenennen fehlgeschlagen");
                alert.setHeaderText("Name bereits vergeben");
                alert.setContentText("Ein Account mit dem gewählten Namen existiert bereits.");
                alert.showAndWait();
                actionEvent.consume();

                renameField.setText(account.getName());
                return;
            }
            account.setName(newName);
        }

        account.setDescription(descriptionTextArea.getText().strip());
        account.setAllowPassiveGameJoining(cbPassiveGameJoining.isSelected());
        if (!newPasswordField.getText().isEmpty()) {
            if (newPasswordField.getText().equals(oldPasswordField.getText())) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Passwort ändern fehlgeschlagen");
                alert.setHeaderText("Passwörter sind identisch");
                alert.setContentText("Das neue Passwort ist identisch mit dem alten.");
                alert.showAndWait();
                actionEvent.consume();
                return;
            }
            if (!account.authenticate(oldPasswordField.getText())) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Passwort ändern fehlgeschlagen");
                alert.setHeaderText("Falsches Passwort");
                alert.setContentText("Das alte Passwort ist falsch.");
                alert.showAndWait();
                actionEvent.consume();
                return;
            }
            account.updatePassword(newPasswordField.getText());
        }
    }
}