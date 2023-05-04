package org.app.fx_application.selectables;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;

import org.app.GameRole;

import java.util.UUID;

public class SelectableWhitelistEntry extends HBox {
    private final Label accountNameLabel;
    private final ComboBox<GameRole> cboxRole;
    private final Button removeButton;

    private final UUID gameId;

    public SelectableWhitelistEntry(UUID gameId, String accountName, byte role) {
        super(5);
        setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        setMinWidth(295);
        javafx.scene.layout.VBox.setVgrow(this, javafx.scene.layout.Priority.ALWAYS);
        getStyleClass().add("clickable-pane");

        this.gameId = gameId;
        accountNameLabel = new Label(accountName);
        accountNameLabel.setFont(javafx.scene.text.Font.font(13));

        cboxRole = new ComboBox<>();
        for (int i = 1; i < GameRole.values().length - 1; i++) {
            cboxRole.getItems().add(GameRole.getRole(i));
        }
        cboxRole.setValue(GameRole.getRole(role));
        cboxRole.setConverter(GameRole.STRING_CONVERTER);

        removeButton = new Button("Entfernen");
        removeButton.getStyleClass().add("no-skin-button");

        getChildren().addAll(accountNameLabel, cboxRole, removeButton);
    }

    public void setOnRemove(EventHandler<ActionEvent> handler) {
        removeButton.setOnAction(handler);
    }
    public void setOnChange(EventHandler<ActionEvent> handler) {
        cboxRole.setOnAction(handler);
    }
    public String getAccountName() {
        return accountNameLabel.getText();
    }
    public GameRole getRole() {
        return cboxRole.getValue();
    }
}