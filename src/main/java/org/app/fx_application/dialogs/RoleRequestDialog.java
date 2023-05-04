package org.app.fx_application.dialogs;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import org.app.GameRole;
import org.app.GameMetadata;
import org.app.fx_application.selectables.SelectableOutgoingRequest;

public class RoleRequestDialog extends CustomDialog<SelectableOutgoingRequest> {
    public static final int MAX_MESSAGE_LENGTH = 80;

    @FXML private Label requestRoleLabel;
    private Label messageExceedLabel;
    @FXML private ComboBox<GameRole> cboxRole;
    @FXML private TextArea messageArea;
    @FXML private VBox vbMain;

    private GameMetadata metadata;
    private SelectableOutgoingRequest request;

    public RoleRequestDialog() {
        super();
        Button bConfirm = (Button) getDialogPane().lookupButton(ButtonType.OK);
        bConfirm.setText("Senden");
        bConfirm.addEventFilter(ActionEvent.ACTION, this::onConfirm);
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

    public void setMetadata(GameMetadata metadata) {
        this.metadata = metadata;
        requestRoleLabel.setText("Rolle bei " + metadata.getName() + " anfragen:");

        // Füge alle Rollen zur ComboBox hinzu, die höher sind als die aktuelle Rolle des Accounts
        byte currentRoleValue = metadata.getWhitelistedRole();
        for (byte i = (byte) (currentRoleValue + 1); i <= 3; i++) {
            cboxRole.getItems().add(GameRole.getRole(i));
        }
        cboxRole.getSelectionModel().selectFirst();
    }

    private void onConfirm(ActionEvent actionEvent) {
        request = new SelectableOutgoingRequest(metadata.getAccount().getName(), metadata.getName(),
                metadata.getNumSuffix(), cboxRole.getValue().getValue(), messageArea.getText(), null, // null = Ausstehend
                metadata.getId(), metadata.getType().getValue());
    }
}