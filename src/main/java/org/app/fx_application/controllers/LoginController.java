package org.app.fx_application.controllers;

import jakarta.mail.MessagingException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import org.app.fx_application.daos.AccountDao;
import org.app.fx_application.EmailSender;
import org.app.fx_application.JdbiProvider;
import org.app.fx_application.SceneLoader;
import org.jdbi.v3.core.Jdbi;

import org.app.game_classes.Account;

import java.util.List;
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

    private Jdbi jdbi;

    private static final int REG_CODE_TIMEOUT = 5 * 60 * 1000;
    private Timer regCodeTimer;
    private TimerTask regCodeTimerTask;

    public void initialize() {
        infoAlert.setTitle("Game-Manager Info");
        errorAlert.setTitle("Fehler");
        warningAlert.setTitle("Achtung");
        currentRegCode = null;
        regCodeTimer = new Timer();
        createRegCodeTimerTask();
        currentEmail = null;
        currentAccount = null;

        unameRegField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 20) {
                unameRegField.setText(oldValue);
            }
        });
        unameLoginField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 20) {
                unameLoginField.setText(oldValue);
            }
        });
        emailRegField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 50) {
                emailRegField.setText(oldValue);
            }
        });
        regCodeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 6 || !newValue.matches("[0-9]*")) {
                regCodeField.setText(oldValue);
            }
        });

        jdbi = JdbiProvider.getInstance().getJdbi();
        int abandonedAccountMonthsTreshold = 8;
        jdbi.useHandle(handle -> {
            AccountDao dao = handle.attach(AccountDao.class);
            List<String> abandonedAccountNames = dao.getAbandonedAccountNames(abandonedAccountMonthsTreshold);
            for (String abandonedAccountName : abandonedAccountNames) {
                dao.deleteAccount(abandonedAccountName);
            }
        });
    }

    @FXML
    public void onUnameLoginFieldEnter() {
        if (unameLoginField.getText().isEmpty())
            unameLoginField.requestFocus();
        else passwordLoginField.requestFocus();
    }
    @FXML
    public void onPasswordLoginFieldEnter() {
        if (passwordLoginField.getText().isEmpty())
            passwordLoginField.requestFocus();
        else {
            loginButton.requestFocus();
            loginButton.fire();
        }
    }

    @FXML
    public void onEmailRegFieldEnter() {
        if (emailRegField.getText().isEmpty())
            emailRegField.requestFocus();
        else {
            sendRegCodeButton.requestFocus();
            sendRegCodeButton.fire();
        }
    }
    @FXML
    public void onUnameRegFieldEnter() {
        if (unameRegField.getText().isEmpty())
            unameRegField.requestFocus();
        else passwordRegField.requestFocus();
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
        else regCodeField.requestFocus();
    }
    @FXML
    public void onRegCodeFieldEnter() {
        if (regCodeField.getText().isEmpty())
            regCodeField.requestFocus();
        else {
            registerButton.requestFocus();
            registerButton.fire();
        }
    }

    @FXML
    public void onLoginButtonClicked() {
        if (unameLoginField.getText().isEmpty() || passwordLoginField.getText().isEmpty()) {
            errorAlert.setHeaderText("Fehlende Anmeldedaten");
            errorAlert.setContentText("Bitte geben Sie einen Benutzernamen und ein Passwort ein, um sich anzumelden.");
            errorAlert.showAndWait();
            return;
        }
        String uname, password;
        unameLoginField.setText(uname = unameLoginField.getText().strip());
        passwordLoginField.setText(password = passwordLoginField.getText().strip());

        Account account = jdbi.withHandle(handle -> (handle.attach(AccountDao.class).getByName(uname)));
        if (account == null) {
            errorAlert.setHeaderText("Ungültiger Benutzername");
            errorAlert.setContentText("Der angegebene Benutzername existiert nicht. Registrieren Sie sich, um einen neuen Account zu erstellen.");
            errorAlert.showAndWait();
            return;
        }
        if (!account.authenticate(password)) {
            errorAlert.setHeaderText("Ungültiges Passwort");
            errorAlert.setContentText("Das angegebene Passwort ist falsch. Bitte versuchen Sie es erneut.");
            errorAlert.showAndWait();
            return;
        }

        currentAccount = account;
        System.out.println("Erstellungsdatum des eingeloggten Accounts: " + currentAccount.getDateCreated());
        jdbi.useHandle(handle -> handle.attach(AccountDao.class).updateLastLogin(currentAccount.getName()));
        openMainMenuScene();
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

        boolean existsWithEmail = jdbi.withHandle(handle -> handle.attach(AccountDao.class).existsByEmail(email));
        if (existsWithEmail) {
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
            errorAlert.setContentText("Ihr Registrierungscode ist abgelaufen oder wurde noch nicht angefordert. Bitte fordern Sie einen Registrierungscode an.");
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
        boolean existsWithUname = jdbi.withHandle(handle -> handle.attach(AccountDao.class).existsByName(uname));
        if (existsWithUname) {
            errorAlert.setHeaderText("Benutzername bereits vergeben");
            errorAlert.setContentText("Dieser Benutzername ist bereits vergeben. Bitte wählen Sie einen anderen Benutzernamen.");
            errorAlert.showAndWait();
            return;
        }

        String password = passwordRegField.getText().strip();
        currentAccount = Account.fromRawPassword(uname, password, currentEmail, null, false);

        jdbi.useHandle(handle -> {
            AccountDao accountDao = handle.attach(AccountDao.class);
            accountDao.saveNewAccount(currentAccount);
            currentAccount = accountDao.getByName(uname);
        });
        openMainMenuScene();
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
        createRegCodeTimerTask();
        regCodeTimer.schedule(regCodeTimerTask, REG_CODE_TIMEOUT);
    }
    private void createRegCodeTimerTask() {
        regCodeTimerTask = new TimerTask() {
            @Override
            public void run() {
                currentRegCode = null;
                warningAlert.setHeaderText("Registrierungscode abgelaufen");
                warningAlert.setContentText("Ihr Registrierungscode ist abgelaufen. Bitte fordern Sie einen neuen an.");
                warningAlert.showAndWait();
            }
        };
    }

    public void openMainMenuScene() {
        SceneLoader.openMainMenuScene((Stage) loginButton.getScene().getWindow(), currentAccount);
    }
}