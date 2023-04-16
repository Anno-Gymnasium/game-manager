package org.app.fx_application.dialogs;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.app.game_classes.Account;

import java.io.IOException;
import java.util.Objects;

/** Allgemeiner Dialog, der die Initialisierung der FXML-Datei als DialogPane übernimmt. */
public abstract class CustomDialog<R> extends Dialog<R> implements Initializable {
    @Override
    public void initialize(java.net.URL location, java.util.ResourceBundle resources) {}

    public CustomDialog() {
        super();
        setDialogPane(loadDialogPane());
        getDialogPane().getScene().getWindow().setOnCloseRequest(windowEvent -> {
            windowEvent.consume();
            onCancel();
        });
        Button bCancel = (Button) getDialogPane().lookupButton(ButtonType.CANCEL);
        if (bCancel != null) {
            bCancel.setOnAction(actionEvent -> onCancel());
        }
    }

    protected DialogPane loadDialogPane(String fxmlPath) {
        FXMLLoader dialogLoader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource(fxmlPath)));
        try {
            dialogLoader.setRoot(getDialogPane());
            dialogLoader.setController(this);
            return dialogLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    protected abstract DialogPane loadDialogPane();
    protected void onCancel() {
        setResult(null);
        close();
    }

    public static void openAccountInfoAlert(Account account) {
        Alert accountInfo = new Alert(Alert.AlertType.INFORMATION);
        accountInfo.setTitle("Account-Info");
        accountInfo.setHeaderText(account.getName());
        accountInfo.setGraphic(null);

        TextArea descriptionArea = new TextArea(account.getDescription());
        descriptionArea.setWrapText(true);
        descriptionArea.setPromptText("Keine Beschreibung vorhanden");
        descriptionArea.setEditable(false);
        accountInfo.getDialogPane().setContent(descriptionArea);
        accountInfo.showAndWait();
    }
}