package org.app.fx_application.selectables;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import org.app.GameMetadata;
import org.app.fx_application.dialogs.GamePreviewDialog;
import org.app.fx_application.dialogs.GameEditDialogResult;
import org.app.game_classes.GenericGame;

/** Vorschau eines Spiels in der Spielübersicht im Hauptmenü. */
public class GamePreview extends HBox implements Comparable<GamePreview> {
    public static final int SHORT_DESCRIPTION_LENGTH = 20;
    public static final int MAX_NAME_LENGTH = GenericGame.MAX_NAME_LENGTH;
    public static final int MAX_DESCRIPTION_LENGTH = GenericGame.MAX_DESCRIPTION_LENGTH;

    private final Label nameLabel, roleLabel, descriptionLabel;
    private final GameMetadata metadata;
    private Runnable onDeleteGame;

    public GamePreview(GameMetadata metadata) {
        super();
        this.metadata = metadata;
        VBox.setVgrow(this, Priority.ALWAYS);
        getStyleClass().add("clickable-pane");
        setSpacing(3);
        setMinWidth(290);
        setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        nameLabel = new Label();
        nameLabel.setFont(Font.font(13));
        updateNameLabel();

        roleLabel = new Label("(" + metadata.getAccountRole().getName() + ")");
        roleLabel.setFont(Font.font(12));
        roleLabel.setOpacity(0.8);

        descriptionLabel = new Label();
        descriptionLabel.setFont(Font.font(12));
        descriptionLabel.setOpacity(0.8);
        updateDescriptionLabel();

        Button previewButton = new Button("Vorschau");
        previewButton.getStyleClass().add("no-skin-button");
        previewButton.setOnAction(event -> openDialog());

        getChildren().addAll(nameLabel, roleLabel, descriptionLabel, previewButton);

        setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                openDialog();
            }
        });
    }

    public GameMetadata getMetadata() {
        return metadata;
    }

    public void updateNameLabel() {
        nameLabel.setText(metadata.getCompositeName());
    }
    public void updateDescriptionLabel() {
        String description = metadata.getDescription();
        String shortenedDescription = description.length() > SHORT_DESCRIPTION_LENGTH ?
                description.substring(0, SHORT_DESCRIPTION_LENGTH) + "..." : description;
        descriptionLabel.setText(shortenedDescription);
    }
    public static String getCompositeName(String name, int numSuffix) {
        return name + (numSuffix > 0 ? (" #" + numSuffix) : "");
    }

    public GameEditDialogResult openDialog(Window owner) {
        GamePreviewDialog dialog = new GamePreviewDialog();
        dialog.setMetadata(metadata);
        dialog.initOwner(owner);

        dialog.showAndWait();
        GameEditDialogResult result = dialog.getResult();
        if (result.gameDeleted()) { // Spiel wurde gelöscht
            System.out.println("Spiel wurde gelöscht");
            // Event.fireEvent(owner, new Event(MainMenuController.RELOAD));
            if (onDeleteGame != null) {
                onDeleteGame.run();
            }
            return result;
        }
        updateDescriptionLabel();
        if (result.gameChanged()) { // Spielname wurde geändert
            // Event.fireEvent(owner, new Event(MainMenuController.RELOAD));
            updateNameLabel();
            updateDescriptionLabel();
        }
        return result;
    }
    public GameEditDialogResult openDialog() {
        return openDialog(getScene().getWindow());
    }
    public static GameEditDialogResult openDialog(Window owner, GameMetadata metadata) {
        if (metadata == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(owner);
            alert.setTitle("Zugriffsfehler");
            alert.setHeaderText("Zugriff auf Spiel nicht möglich");
            alert.setContentText("Entweder wurde Ihre Berechtigung zum Zugriff auf das Spiel entfernt oder es wurde vor Kurzem gelöscht.");
            alert.showAndWait();
            return new GameEditDialogResult(false, false);
        }
        return new GamePreview(metadata).openDialog(owner);
    }

    public void setOnDeleteGame(Runnable onDeleteGame) {
        this.onDeleteGame = onDeleteGame;
    }

    @Override
    public int compareTo(GamePreview o) {
        return metadata.compareTo(o.metadata);
    }
}