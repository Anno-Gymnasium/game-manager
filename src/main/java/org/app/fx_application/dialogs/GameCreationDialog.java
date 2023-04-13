package org.app.fx_application.dialogs;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import org.app.game_classes.GenericGame;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Objects;

public class GameCreationDialog<G extends GenericGame<?, ?>> extends CustomDialog<G> {
    @FXML private Label createGameLabel;
    @FXML private TextField gameNameField;
    @FXML private TextArea descriptionTextArea;
    @FXML private CheckBox cbSoloTeams, cbPublicView, cbOwnTeamsCreation;

    private Constructor<G> gameConstructor;
    private G game;

    public GameCreationDialog() {
        super();
        getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.APPLY);
        Button bCancel = (Button) getDialogPane().lookupButton(ButtonType.CANCEL);
        Button bConfirm = (Button) getDialogPane().lookupButton(ButtonType.APPLY);
        bCancel.setOnAction(actionEvent -> onCancel());
        bConfirm.addEventFilter(ActionEvent.ACTION, this::onConfirm);

        setResultConverter(buttonType -> {
            if (buttonType == ButtonType.APPLY) {
                return game;
            }
            return null;
        });
    }

    protected DialogPane loadDialogPane() {
        return loadDialogPane("game-creation-dialog.fxml");
    }

    public void setGameClass(Class<G> gameClass) {
        String gameType;
        if (gameClass == org.app.game_classes.MatchlessGame.class) {
            gameType = "All-vs-All-Spiel";
        } else if (gameClass == org.app.game_classes.MatchingGame.class) {
            gameType = "Match-Spiel";
        } else if (gameClass == org.app.game_classes.TreeGame.class) {
            gameType = "Baum-Spiel";
        } else {
            throw new IllegalArgumentException("Ungültige Spielklasse");
        }
        createGameLabel.setText(gameType + " erstellen");

        try {
            gameConstructor = gameClass.getConstructor(boolean.class, boolean.class, boolean.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private void onConfirm(ActionEvent actionEvent) {
        try {
            game = gameConstructor.newInstance(cbSoloTeams.isSelected(), cbPublicView.isSelected(), cbOwnTeamsCreation.isSelected());
            game.setName(gameNameField.getText().strip());
            game.setDescription(descriptionTextArea.getText().strip());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}