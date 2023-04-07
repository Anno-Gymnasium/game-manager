package org.app.fx_application;

import jakarta.mail.MessagingException;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import org.app.game_classes.Account;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class LoginController {

    @FXML private TextField unameLoginField;
    @FXML private TextField unameRegField, emailRegField;
    @FXML private TextField regCodeField;

    @FXML private PasswordField passwordLoginField, passwordRegField, passwordConfirmField;

    @FXML private Button loginButton, sendRegCodeButton, registerButton;

    private Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
    private Alert errorAlert = new Alert(Alert.AlertType.ERROR);
    private Alert warningAlert = new Alert(Alert.AlertType.WARNING);
    private String currentRegCode;

    private String currentEmail;
    private Account currentAccount;

    private EntityManagerFactory entityManagerFactory;
    private AccountManager accountManager;

    private static final int REG_CODE_TIMEOUT = 5 * 60 * 1000;
    private Timer regCodeTimer;
    private TimerTask regCodeTimerTask;
    private Scene gameSelectionScene;
    private GameSelectionController gameSelectionController;

    public void initialize() {
        infoAlert.setTitle("Game-Manager Info");
        errorAlert.setTitle("Fehler");
        warningAlert.setTitle("Achtung");
        currentRegCode = null;
        regCodeTimer = new Timer();
        regCodeTimerTask = new TimerTask() {
            @Override
            public void run() {
                currentRegCode = null;
                warningAlert.setHeaderText("Registrierungscode abgelaufen");
                warningAlert.setContentText("Ihr Registrierungscode ist abgelaufen. Bitte fordern Sie einen neuen an.");
                warningAlert.showAndWait();
            }
        };
        currentEmail = null;
        currentAccount = null;
        gameSelectionScene = null;
        gameSelectionController = null;
        entityManagerFactory = Persistence.createEntityManagerFactory("default");
        accountManager = new AccountManager(entityManagerFactory.createEntityManager());
    }

    @FXML
    public void onUnameLoginFieldEnter() {
        if (unameLoginField.getText().isEmpty())
            unameLoginField.requestFocus();
        else
            passwordLoginField.requestFocus();
    }
    @FXML
    public void onPasswordLoginFieldEnter() {
        if (passwordLoginField.getText().isEmpty())
            passwordLoginField.requestFocus();
        else
            loginButton.fire();
    }

    @FXML
    public void onUnameRegFieldEnter() {
        if (unameRegField.getText().isEmpty())
            unameRegField.requestFocus();
        else
            emailRegField.requestFocus();
    }
    @FXML
    public void onEmailRegFieldEnter() {
        if (emailRegField.getText().isEmpty())
            emailRegField.requestFocus();
        else
            passwordRegField.requestFocus();
    }
    @FXML
    public void onPasswordRegFieldEnter() {
        if (passwordRegField.getText().isEmpty())
            passwordRegField.requestFocus();
        else
            passwordConfirmField.requestFocus();
    }
    @FXML
    public void onPasswordConfirmFieldEnter() {
        if (passwordConfirmField.getText().isEmpty())
            passwordConfirmField.requestFocus();
        else
            sendRegCodeButton.fire();
    }
    @FXML
    public void onRegCodeFieldEnter() {
        if (regCodeField.getText().isEmpty())
            regCodeField.requestFocus();
        else
            registerButton.fire();
    }

    @FXML
    public void onLoginButtonClicked() {
        // TODO: Login-Vorgang implementieren
    }
    @FXML
    public void onSendRegCodeButtonClicked() {
        if (emailRegField.getText().isEmpty()) {
            errorAlert.setHeaderText("Fehlende E-Mail-Adresse");
            errorAlert.setContentText("Bitte eine E-Mail-Adresse eingeben, um einen Registrierungscode zu erhalten.");
            errorAlert.showAndWait();
            return;
        }
        String email;
        emailRegField.setText(email = emailRegField.getText().strip());

        String emailValidationRegex = "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        if (!email.matches(emailValidationRegex)) {
            errorAlert.setHeaderText("Ungültige E-Mail-Adresse");
            errorAlert.setContentText("Bitte eine gültige E-Mail-Adresse eingeben, um einen Registrierungscode zu erhalten.");
            errorAlert.showAndWait();
            return;
        }
        if (accountManager.getByEmail(email) != null) {
            errorAlert.setHeaderText("E-Mail-Adresse bereits registriert");
            errorAlert.setContentText("Diese E-Mail-Adresse ist bereits registriert. Bitte melden Sie sich an oder verwenden Sie eine andere E-Mail-Adresse.");
            errorAlert.showAndWait();
            return;
        }

        generateRandomRegCode();
        String subject = "Game-Manager Registrierung";
        String message = "Ihr Registrierungscode lautet: " + currentRegCode;
        try {
            EmailSender.sendEmail(email, subject, message);
            currentEmail = email;
            infoAlert.setHeaderText("Registrierungscode erfolgreich versendet");
            infoAlert.setContentText("Bitte überprüfen Sie Ihr Postfach und geben Sie den Registrierungscode ein." +
                    "\nDer Code läuft nach 5 Minuten ab. Überprüfen Sie ggf. auch Ihren Spam-Ordner.");
            infoAlert.showAndWait();
        } catch (MessagingException e) {
            errorAlert.setHeaderText("Fehler beim Senden des Registrierungscodes");
            errorAlert.setContentText("Bitte überprüfen Sie Ihre Internetverbindung und versuchen Sie es erneut.");
            errorAlert.showAndWait();
        }
    }
    @FXML
    public void onRegisterButtonClicked() {
        if (unameRegField.getText().isEmpty() || emailRegField.getText().isEmpty() || passwordRegField.getText().isEmpty() ||
                passwordConfirmField.getText().isEmpty() || regCodeField.getText().isEmpty()) {
            errorAlert.setHeaderText("Unvollständige Registrierungsdaten");
            errorAlert.setContentText("Bitte alle Felder zur Registrierung ausfüllen.");
            errorAlert.showAndWait();
            return;
        }
        if (!passwordRegField.getText().equals(passwordConfirmField.getText())) {
            errorAlert.setHeaderText("Passwörter stimmen nicht überein");
            errorAlert.setContentText("Bitte zweimal das gleiche Passwort eingeben.");
            errorAlert.showAndWait();
            return;
        }
        if (currentRegCode == null) {
            errorAlert.setHeaderText("Registrierungscode abgelaufen");
            errorAlert.setContentText("Ihr Registrierungscode ist abgelaufen. Bitte fordern Sie einen neuen an.");
            errorAlert.showAndWait();
            return;
        }
        if (!regCodeField.getText().equals(currentRegCode)) {
            errorAlert.setHeaderText("Ungültiger Registrierungscode");
            errorAlert.setContentText("Bitte überprüfen Sie den Registrierungscode und versuchen Sie es erneut.");
            errorAlert.showAndWait();
            return;
        }

        String uname = unameRegField.getText().strip();
        if (accountManager.getByName(uname) != null) {
            errorAlert.setHeaderText("Benutzername bereits vergeben");
            errorAlert.setContentText("Dieser Benutzername ist bereits vergeben. Bitte wählen Sie einen anderen Benutzernamen.");
            errorAlert.showAndWait();
            return;
        }

        String password = passwordRegField.getText().strip();
        currentAccount = new Account(uname, password, currentEmail);

        accountManager.saveNewAccount(currentAccount);
    }

    private void generateRandomRegCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int randomInt = random.nextInt(10);
            sb.append(randomInt);
        }
        currentRegCode = sb.toString();

        regCodeTimer.cancel();
        regCodeTimer = new Timer();
        regCodeTimer.schedule(regCodeTimerTask, REG_CODE_TIMEOUT);
    }

    public void setGameSelectionScene(Scene scene) {
        gameSelectionScene = scene;
    }
    public void setGameSelectionController(GameSelectionController controller) {
        gameSelectionController = controller;
    }
    public void openGameSelectionScene() {
        if (gameSelectionScene == null)
            return;
        gameSelectionController.setAccount(currentAccount);
        Stage primaryStage = (Stage) loginButton.getScene().getWindow();
        primaryStage.setScene(gameSelectionScene);
    }
}