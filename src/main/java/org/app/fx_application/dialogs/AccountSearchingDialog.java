package org.app.fx_application.dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.util.List;

public abstract class AccountSearchingDialog<R> extends CustomDialog<R> {
    @FXML protected VBox vbAccounts;
    protected Label selectedAccountLabel = null;
    @FXML protected TextField searchAccountField;
    protected boolean searchQueryChanged = false;
    @FXML protected Button bSearchAccount, bShowAccountInfo;

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

        searchAccountField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchQueryChanged = true;
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
        if (!searchQueryChanged) return;
        searchQueryChanged = false;

        selectedAccountLabel = null;
        vbAccounts.getChildren().clear();
        bShowAccountInfo.setDisable(true);
        String query = searchAccountField.getText().strip();
        if (query.isEmpty()) {
            return;
        }
        getQueriedAccountNames(query).forEach(accountName -> {
                    Label accountLabel = new Label(accountName);
                    accountLabel.setFont(javafx.scene.text.Font.font(13));
                    accountLabel.setMinWidth(295);
                    accountLabel.getStyleClass().add("clickable-pane");
                    accountLabel.setOnMouseClicked(event -> {
                        onLabelClick(accountLabel);
                    });
                    vbAccounts.getChildren().add(accountLabel);
                });
        if (!vbAccounts.getChildren().isEmpty()) {
            triggerMouseClick((Label) vbAccounts.getChildren().get(0));
        }
    }
    protected abstract void beforeSearchAccounts();
    protected abstract List<String> getQueriedAccountNames(String query);
    protected abstract void onLabelClick(Label label);

    private void triggerMouseClick(Label label) {
        label.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY,
                1, true, true, true, true, true, true, true, true, true, true, null));
    }
}