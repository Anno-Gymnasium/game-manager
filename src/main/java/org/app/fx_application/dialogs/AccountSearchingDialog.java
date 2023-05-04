package org.app.fx_application.dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.util.List;

import org.app.fx_application.daos.AccountDao;
import org.app.game_classes.Account;

import org.app.fx_application.JdbiProvider;
import org.jdbi.v3.core.Jdbi;

public abstract class AccountSearchingDialog<R> extends CustomDialog<R> {
    @FXML protected VBox vbAccounts;
    protected Label selectedAccountLabel = null;
    @FXML protected TextField searchAccountField;
    protected boolean accountQueryChanged = false;
    @FXML protected Button bSearchAccount, bShowAccountInfo;

    protected Jdbi jdbi = JdbiProvider.getInstance().getJdbi();

    public AccountSearchingDialog() {
        super();
        bShowAccountInfo.setDisable(true);

        // Listener zur Navigation durch die Liste der Accounts mit den Pfeiltasten
        vbAccounts.setOnKeyPressed(keyEvent -> {
            switch (keyEvent.getCode()) {
                case UP -> selectPreviousAccount();
                case DOWN -> selectNextAccount();
            }
        });

        searchAccountField.textProperty().addListener((obs, o, n) -> {
            if (n.length() > Account.MAX_NAME_LENGTH) {
                searchAccountField.setText(o);
                return;
            }
            accountQueryChanged = true;
        });
        searchAccountField.requestFocus();
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

    @FXML
    private void onSearchFieldEnter() {
        bSearchAccount.requestFocus();
        bSearchAccount.fire();
    }
    @FXML
    private void onSearchAccounts() {
        if (!accountQueryChanged) return;
        accountQueryChanged = false;

        beforeSearchAccounts();

        selectedAccountLabel = null;
        vbAccounts.getChildren().clear();
        bShowAccountInfo.setDisable(true);
        String query = searchAccountField.getText().strip();
        getQueriedAccountNames(query).forEach(accountName -> {
                    Label accountLabel = new Label(accountName);
                    accountLabel.setFont(javafx.scene.text.Font.font(13));
                    accountLabel.setMinWidth(295);
                    accountLabel.getStyleClass().add("clickable-pane");
                    accountLabel.setOnMouseClicked(event -> {
                        if (selectedAccountLabel == accountLabel) return;
                        selectedAccountLabel = accountLabel;
                        accountLabel.setStyle("-fx-background-color: #3cb8d4");
                        bShowAccountInfo.setDisable(false);
                        onLabelClick(accountLabel.getText());
                    });
                    vbAccounts.getChildren().add(accountLabel);
                });
        if (!vbAccounts.getChildren().isEmpty()) {
            triggerMouseClick((Label) vbAccounts.getChildren().get(0));
        }
    }

    protected Account loadSelectedAccount() {
        return jdbi.withExtension(AccountDao.class, dao -> dao.getByName(selectedAccountLabel.getText()));
    }

    protected abstract void beforeSearchAccounts();
    protected abstract List<String> getQueriedAccountNames(String query);
    protected abstract void onLabelClick(String accountName);
    @FXML protected abstract void onShowAccountInfo();

    private void triggerMouseClick(Label label) {
        label.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY,
                1, true, true, true, true, true, true, true, true, true, true, null));
    }
}