package org.app.fx_application.selectables;

import javafx.geometry.Pos;
import javafx.stage.Stage;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import org.app.GameRole;
import org.app.GameType;
import org.app.fx_application.SceneLoader;
import org.app.fx_application.dialogs.GamePreviewDialog;
import org.app.game_classes.Account;

import org.app.game_classes.WhiteListEntry;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

import java.util.UUID;

public class PreviewGame extends HBox {
    private final Label nameLabel, roleLabel, descriptionLabel;

    private final UUID gameId;
    private final GameType type;
    private String name;
    private int numSuffix;
    private String description;
    private Account currentAccount;
    private final GameRole accountRole;
    private final byte whitelistedRole;

    private final boolean soloTeams;
    private boolean publicView;
    private boolean ownTeamsCreation;

    public PreviewGame(UUID id, int type, String name, @ColumnName("num_suffix") int numSuffix, String description,
                       @ColumnName("assigned_role") byte accountRole, @ColumnName("wl_role") byte whitelistedRole, @ColumnName("public_view") boolean publicView,
                       @ColumnName("solo_teams") boolean soloTeams, @ColumnName("allow_own_teams_creation") boolean ownTeamsCreation) {
        super();
        VBox.setVgrow(this, Priority.ALWAYS);
        getStyleClass().add("clickable_pane");
        setMinWidth(260);
        setSpacing(3);

        this.gameId = id;
        this.type = GameType.getType(type);
        this.name = name.strip();
        this.numSuffix = numSuffix;
        this.description = description;
        this.accountRole = GameRole.getRole(accountRole);
        this.whitelistedRole = whitelistedRole;
        this.publicView = publicView;
        this.soloTeams = soloTeams;
        this.ownTeamsCreation = ownTeamsCreation;

        nameLabel = new Label();
        nameLabel.setFont(Font.font(13));
        setGameName(name);

        roleLabel = new Label("(" + this.accountRole.getName() + ")");
        roleLabel.setFont(Font.font(12));
        roleLabel.setOpacity(0.8);

        descriptionLabel = new Label();
        descriptionLabel.setFont(Font.font(12));
        descriptionLabel.setOpacity(0.8);
        setGameDescription(description);

        getChildren().addAll(nameLabel, roleLabel, descriptionLabel);

        setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                openDialog();
            }
        });
    }

    public UUID getGameId() {
        return gameId;
    }
    public String getGameType() {
        return type.getName();
    }
    public String getGameName() {
        return name;
    }
    public int getGameNumSuffix() {
        return numSuffix;
    }
    public String getGameDescription() {
        return description;
    }
    public GameRole getAccountRole() {
        return accountRole;
    }
    public byte getWhitelistedRole() {
        return whitelistedRole;
    }
    public boolean isPublicView() {
        return publicView;
    }
    public boolean isSoloTeams() {
        return soloTeams;
    }
    public boolean isAllowOwnTeamsCreation() {
        return ownTeamsCreation;
    }

    public void setGameName(String name) {
        this.name = name;
        updateNameLabel();
    }
    public void setGameNumSuffix(int numSuffix) {
        this.numSuffix = numSuffix;
        updateNameLabel();
    }
    public void setGameDescription(String description) {
        this.description = description;
        updateDescriptionLabel();
    }
    public void setPublicView(boolean publicView) {
        this.publicView = publicView;
    }
    public void setOwnTeamsCreation(boolean ownTeamsCreation) {
        this.ownTeamsCreation = ownTeamsCreation;
    }
    public void setAccount(Account currentAccount) {
        this.currentAccount = currentAccount;
    }

    private void updateNameLabel() {
        nameLabel.setText(name + (numSuffix > 0 ? (" #" + numSuffix) : ""));
    }
    private void updateDescriptionLabel() {
        String shortenedDescription = description.length() > 30 ? description.substring(0, 30) + "..." : description;
        descriptionLabel.setText(shortenedDescription);
    }

    public void openDialog() {
        GamePreviewDialog dialog = new GamePreviewDialog();
        dialog.setAccount(currentAccount);
        dialog.setPreviewGame(this);

        dialog.showAndWait().ifPresent(this::joinGame);
    }
    public void joinGame(WhiteListEntry whiteListEntry) {
        Stage stage = (Stage) getScene().getWindow();
        SceneLoader.openGameScene(type, stage, gameId, currentAccount, GameRole.getRole(whiteListEntry.assignedRole()));
    }
}