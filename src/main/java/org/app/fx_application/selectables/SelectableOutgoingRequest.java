package org.app.fx_application.selectables;

import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import org.app.GameRole;

import org.app.fx_application.RequestDao;
import org.app.fx_application.JdbiProvider;
import org.jdbi.v3.core.Jdbi;

public class SelectableOutgoingRequest extends HBox {
    private final Label gameNameLabel, roleLabel;
    private final Button deleteButton;

    private final String accountName;
    private final GameRole role;
    private final String gameName;
    private final int gameNumSuffix;
    private final String message;
    private final Jdbi jdbi = JdbiProvider.getInstance().getJdbi();

    public SelectableOutgoingRequest(String accountName, String gameName, int gameNumSuffix, byte role, String message) {
        super();
        VBox.setVgrow(this, Priority.ALWAYS);
        getStyleClass().add("clickable_pane");
        setMinWidth(260);
        setSpacing(5);
        setAlignment(Pos.CENTER_LEFT);

        this.accountName = accountName;
        this.role = GameRole.getRole(role);
        this.gameName = gameName;
        this.gameNumSuffix = gameNumSuffix;
        this.message = message;

        gameNameLabel = new Label(gameName + (gameNumSuffix > 0 ? (" #" + gameNumSuffix) : ""));
        gameNameLabel.setFont(Font.font(13));

        roleLabel = new Label(this.role.getName());
        roleLabel.setFont(Font.font(12));
        roleLabel.setOpacity(0.8);

        deleteButton = new Button("Löschen");
        deleteButton.setFont(Font.font(11));
        deleteButton.setOnAction(this::onDelete);

        getChildren().addAll(gameNameLabel, roleLabel, deleteButton);
    }

    private void onDelete(ActionEvent actionEvent) {
        jdbi.useHandle(handle -> handle.attach(RequestDao.class).deleteRequest(this));
        ((VBox) getParent()).getChildren().remove(this);
    }

    public String getAccountName() {
        return accountName;
    }
    public GameRole getRole() {
        return role;
    }
    public String getGameName() {
        return gameName;
    }
    public int getGameNumSuffix() {
        return gameNumSuffix;
    }
    public String getMessage() {
        return message;
    }
}