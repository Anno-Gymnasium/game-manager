package org.app.fx_application.selectables;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.layout.VBox;

import org.app.GameRole;
import org.app.fx_application.daos.RequestDao;
import org.app.fx_application.daos.WhitelistDao;
import org.app.fx_application.dialogs.RequestAnswerDialog;

import java.util.UUID;

public class SelectableIncomingRequest extends SelectableRequest {
    private final UUID gameId;
    private final GameRole currentRole;

    public SelectableIncomingRequest(String accountName, UUID gameId, String gameName, int gameNumSuffix, byte currentRole, byte requestedRole, String message) {
        super(accountName, gameName, gameNumSuffix, requestedRole, message);
        this.gameId = gameId;
        this.currentRole = GameRole.getRole(currentRole);

        Label accountNameLabel = new Label(accountName + ": ");
        accountNameLabel.setFont(Font.font(12));

        roleLabel.setText(this.currentRole.getName() + " -> " + this.requestedRole.getName());

        Button rejectButton = new Button("Ablehnen");
        rejectButton.getStyleClass().add("no-skin-button");
        rejectButton.setOnAction(this::onReject);

        Button acceptButton = new Button("Akzeptieren");
        acceptButton.getStyleClass().add("no-skin-button");
        acceptButton.setOnAction(event -> onAccept());

        getChildren().addAll(accountNameLabel, roleLabel, rejectButton, acceptButton);

        setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                onAccept();
            }
        });
    }

    private void onReject(ActionEvent actionEvent) {
        jdbi.useHandle(handle -> handle.attach(RequestDao.class).rejectRequest(this));
        ((VBox) getParent()).getChildren().remove(this);
    }
    private void onAccept() {
        RequestAnswerDialog dialog = new RequestAnswerDialog();
        dialog.setRequest(this);
        dialog.showAndWait().ifPresent(gameRole -> {
            jdbi.useHandle(handle -> handle.attach(RequestDao.class).acceptRequest(this, gameRole.getValue()));
            jdbi.useHandle(handle -> handle.attach(WhitelistDao.class).setEntry(this.gameId, this.accountName, gameRole.getValue()));
            ((VBox) getParent()).getChildren().remove(this);
        });
    }

    public GameRole getCurrentRole() {
        return currentRole;
    }
}