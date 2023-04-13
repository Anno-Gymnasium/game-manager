package org.app.fx_application.dialogs;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import javafx.stage.Stage;
import org.app.fx_application.AccountDao;
import org.app.fx_application.JdbiProvider;
import org.app.fx_application.SceneLoader;
import org.app.game_classes.Account;
import org.jdbi.v3.core.Jdbi;

import java.io.IOException;
import java.util.Objects;

public class AccountSettingsDialog extends CustomDialog<Account> {
    @FXML private Label accountNameLabel;
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
                return;
            }
            account.setName(newName);
        }

        account.setDescription(descriptionTextArea.getText().strip());
        account.setAllowPassiveGameJoining(cbPassiveGameJoining.isSelected());
        if (!newPasswordField.getText().isEmpty() && !newPasswordField.getText().equals(oldPasswordField.getText())) {
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