package org.app.fx_application.dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import javafx.scene.layout.VBox;
import org.app.GameMetadata;
import org.app.GameRole;
import org.app.game_classes.Account;

public class WhitelistDialog extends AccountSearchingDialog<ButtonType> {
    private Button saveButton;
    @FXML private Label gameNameLabel;
    @FXML private ComboBox<GameRole> cboxRole;
    @FXML private VBox vbEntries;
    @FXML private Button bAddEntry;

    private Account selectedAccount = null;
    private GameMetadata metadata;

    public WhitelistDialog() {
        super();
        saveButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        saveButton.getStyleClass().add("no-skin-button");
        saveButton.setText("Speichern");
        saveButton.setDisable(true);
        bAddEntry.setDisable(true);
        getDialogPane().lookupButton(ButtonType.CLOSE).getStyleClass().add("no-skin-button");
    }
    protected DialogPane loadDialogPane() {
        return loadDialogPane("whitelist-dialog.fxml");
    }
    public void setMetadata(GameMetadata metadata) {
        this.metadata = metadata;
        gameNameLabel.setText("Whitelist von " + metadata.getCompositeName() + " bearbeiten");

    }

    @FXML
    private void onAddEntry() {

    }
}