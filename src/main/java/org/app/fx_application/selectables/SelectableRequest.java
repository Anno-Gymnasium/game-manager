package org.app.fx_application.selectables;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import org.app.GameRole;

import org.app.fx_application.JdbiProvider;
import org.jdbi.v3.core.Jdbi;

public class SelectableRequest extends HBox {
    protected final Label gameNameLabel, roleLabel;
    protected final String accountName, gameName;
    protected final int gameNumSuffix;
    protected final GameRole requestedRole;
    protected final String message;

    protected final Jdbi jdbi = JdbiProvider.getInstance().getJdbi();

    public SelectableRequest(String accountName, String gameName, int gameNumSuffix, byte role, String message) {
        super(5);
        VBox.setVgrow(this, Priority.ALWAYS);
        getStyleClass().add("clickable-pane");
        setMinWidth(295);
        setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        this.accountName = accountName;
        this.requestedRole = GameRole.getRole(role);
        this.gameName = gameName;
        this.gameNumSuffix = gameNumSuffix;
        this.message = message;

        gameNameLabel = new Label(GamePreview.getCompositeName(gameName, gameNumSuffix) + ", ");
        gameNameLabel.setFont(Font.font(13));

        roleLabel = new Label();
        roleLabel.setFont(Font.font(12));
        roleLabel.setOpacity(0.8);

        getChildren().add(gameNameLabel);
    }

    public String getAccountName() {
        return accountName;
    }
    public GameRole getRequestedRole() {
        return requestedRole;
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