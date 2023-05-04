package org.app.fx_application.selectables;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.text.Font;
import javafx.scene.layout.VBox;

import org.app.GameRole;
import org.app.GameMetadata;
import org.app.fx_application.daos.GameMetadataDao;
import org.app.fx_application.daos.RequestDao;
import org.app.fx_application.JdbiProvider;
import org.app.game_classes.Account;
import org.jdbi.v3.core.Jdbi;

import java.util.UUID;

public class SelectableOutgoingRequest extends SelectableRequest {
    private final Jdbi jdbi = JdbiProvider.getInstance().getJdbi();
    private final UUID gameId;
    private final byte gameType;
    private GameMetadata gameMetadata;

    public SelectableOutgoingRequest(String accountName, String gameName, int gameNumSuffix, byte requestedRole, String message, Byte acceptedRole,
                                     UUID gameId, byte gameType) {
        super(accountName, gameName, gameNumSuffix, requestedRole, message);
        this.gameId = gameId;
        this.gameType = gameType;

        roleLabel.setText("Angefragt: " + this.requestedRole.getName() + (acceptedRole != null ? (", zugewiesen: " + GameRole.getRole(acceptedRole).getName()) : ""));

        Button deleteButton = new Button(acceptedRole == null ? "Zurückziehen" : "Ok");
        deleteButton.setFont(Font.font(11));
        deleteButton.getStyleClass().add("no-skin-button");
        deleteButton.setOnAction(this::onDelete);

        Button openPreviewButton = new Button("Spiel anzeigen");
        openPreviewButton.setFont(Font.font(11));
        openPreviewButton.getStyleClass().add("no-skin-button");
        openPreviewButton.setOnAction(event -> {
            openPreviewDialog();
        });
        setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                openPreviewDialog();
            }
        });

        getChildren().addAll(roleLabel, deleteButton, openPreviewButton);
    }

    public void setMetadata(GameMetadata gameMetadata) {
        this.gameMetadata = gameMetadata;
    }
    private void onDelete(ActionEvent actionEvent) {
        jdbi.useExtension(RequestDao.class, dao -> dao.deleteRequest(this));
        ((VBox) getParent()).getChildren().remove(this);
    }
    private void openPreviewDialog() {
        gameMetadata.update();
        GamePreview.openDialog(getScene().getWindow(), gameMetadata);
    }

    public UUID getGameId() {
        return gameId;
    }
}