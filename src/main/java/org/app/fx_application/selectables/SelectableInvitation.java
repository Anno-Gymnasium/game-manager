package org.app.fx_application.selectables;

import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.app.GameMetadata;
import org.app.GameRole;
import org.app.fx_application.daos.InvitationDao;
import org.app.fx_application.dialogs.InvitationInfoDialog;

import java.util.UUID;

import org.app.fx_application.JdbiProvider;
import org.jdbi.v3.core.Jdbi;

public class SelectableInvitation extends HBox {
    public static final int SHORT_MESSAGE_LENGTH = 20;
    private Jdbi jdbi = JdbiProvider.getInstance().getJdbi();

    private final UUID gameId;
    private final String gameName;
    private final GameRole newRole;
    private final String message;
    private final Label gameNameLabel, newRoleLabel, messageLabel;
    private GameMetadata gameMetadata;

    public SelectableInvitation(UUID gameId, String gameName, byte newRole, String message) {
        super(5);
        setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        setMinWidth(295);
        VBox.setVgrow(this, javafx.scene.layout.Priority.ALWAYS);
        getStyleClass().add("clickable-pane");
        this.gameId = gameId;
        this.gameName = gameName;
        this.newRole = GameRole.getRole(newRole);
        this.message = message;

        gameNameLabel = new Label(gameName);
        gameNameLabel.setFont(Font.font(13));

        newRoleLabel = new Label("(" + this.newRole.getName() + ")");
        newRoleLabel.setFont(Font.font(12));
        newRoleLabel.setOpacity(0.8);

        String shortMessage = message.length() > SHORT_MESSAGE_LENGTH
                ? message.substring(0, SHORT_MESSAGE_LENGTH) + "..."
                : message;
        messageLabel = new Label(shortMessage);
        messageLabel.setFont(Font.font(12));

        Button showButton = new Button("Anzeigen");
        showButton.getStyleClass().add("no-skin-button");
        showButton.setOnAction(event -> openInfoDialog());

        getChildren().addAll(gameNameLabel, newRoleLabel, messageLabel, showButton);

        setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                openInfoDialog();
            }
        });
    }
    public void setMetadata(GameMetadata gameMetadata) {
        this.gameMetadata = gameMetadata;
    }

    public void openInfoDialog() {
        InvitationInfoDialog dialog = new InvitationInfoDialog();
        dialog.initOwner(getScene().getWindow());
        dialog.setMetadata(gameMetadata);
        dialog.setInvitation(this);
        dialog.showAndWait().ifPresent(delete -> {
            if (delete) {
                jdbi.useExtension(InvitationDao.class, dao -> dao.deleteInvitation(gameId, gameMetadata.getAccount().getName()));
                ((VBox) getParent()).getChildren().remove(this);
            }
        });
    }

    public UUID getGameId() {
        return gameId;
    }
    public GameRole getNewRole() {
        return newRole;
    }
    public String getMessage() {
        return message;
    }
}