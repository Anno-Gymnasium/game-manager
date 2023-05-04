package org.app.fx_application.dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.app.GameMetadata;
import org.app.fx_application.selectables.GamePreview;
import org.app.fx_application.selectables.SelectableInvitation;

public class InvitationInfoDialog extends CustomDialog<Boolean> {
    private GameMetadata gameMetadata;
    private SelectableInvitation invitation;

    @FXML private Hyperlink hlGameName;
    @FXML private Label newRoleLabel;
    @FXML private TextArea messageArea;
    @FXML private CheckBox cbDeleteInvitation;

    public InvitationInfoDialog() {
        super();
        setResultConverter(buttonType -> cbDeleteInvitation.isSelected());
    }

    public void setMetadata(GameMetadata gameMetadata) {
        this.gameMetadata = gameMetadata;
        hlGameName.setText(gameMetadata.getCompositeName());
    }
    public void setInvitation(SelectableInvitation invitation) {
        this.invitation = invitation;
        newRoleLabel.setText(invitation.getNewRole().getName());
        if (invitation.isDeprecated()) {
            newRoleLabel.setStyle("-fx-text-fill: #ff0000;");
            newRoleLabel.setText(newRoleLabel.getText() + ", ursprünglich eingeladen als " + invitation.getInvitedRole().getName());
        }

        messageArea.setText(invitation.getMessage());
    }

    protected DialogPane loadDialogPane() {
        return loadDialogPane("invitation-info-dialog.fxml");
    }

    private void openPreviewDialog() {
        gameMetadata.update();
        GameEditDialogResult result = GamePreview.openDialog(getOwner(), gameMetadata);
        if (result.gameDeleted()) {
            setResult(true);
            close();
        }
    }
    @FXML private void onGameHyperlink() {
        openPreviewDialog();
    }
}