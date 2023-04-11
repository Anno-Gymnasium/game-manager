package org.app.fx_application;

import org.app.GameRoles;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

import java.util.UUID;

public class SelectableGame extends HBox {
    private UUID id;
    private String name;
    private String description;
    private byte accountRole;

    public SelectableGame(UUID id, String name, String description, byte accountRole) {
        super();
        getStyleClass().add("clickable_pane");
        setMinWidth(260);
        setSpacing(5);

        this.name = name;
        this.description = description;
        this.accountRole = accountRole;

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font(13));

        String roleName = GameRoles.getRole(accountRole).getName();
        Label roleLabel = new Label(" (" + roleName + ")");
        roleLabel.setFont(Font.font(12));
        roleLabel.setOpacity(0.8);

        String shortenedDescription = description.length() > 30 ? description.substring(0, 30) + "..." : description;
        Label descriptionLabel = new Label(shortenedDescription);
        descriptionLabel.setFont(Font.font(12));
        descriptionLabel.setOpacity(0.8);

        getChildren().addAll(nameLabel, roleLabel, descriptionLabel);
    }
}