package org.app.fx_application.dialogs;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javafx.scene.layout.VBox;
import org.app.GameMetadata;
import org.app.GameRole;
import org.app.fx_application.daos.AccountDao;
import org.app.fx_application.daos.WhitelistDao;
import org.app.fx_application.selectables.SelectableWhitelistEntry;
import org.app.game_classes.Account;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class WhitelistDialog extends AccountSearchingDialog<ButtonType> {
    private Button saveButton;
    @FXML private Label gameNameLabel;
    @FXML private ComboBox<GameRole> cboxNewEntryRole;
    @FXML private VBox vbEntries;
    @FXML private Button bAddEntry, bSearchEntries;
    @FXML private MenuButton mbFilterRole;
    @FXML private TextField searchEntryField;
    private boolean entryQueryChanged = true;

    private Account selectedAccount = null;
    private GameMetadata metadata;

    private List<SelectableWhitelistEntry> allEntries = new ArrayList<>();
    private HashMap<String, GameRole> added = new HashMap<>();
    private HashMap<String, GameRole> changed = new HashMap<>();
    private List<String> removed = new ArrayList<>();

    public WhitelistDialog() {
        super();
        saveButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        saveButton.getStyleClass().add("no-skin-button");
        saveButton.setText("Speichern");
        saveButton.setDisable(true);
        saveButton.addEventFilter(ActionEvent.ACTION, this::onSaveEntries);

        bAddEntry.setDisable(true);
        getDialogPane().lookupButton(ButtonType.CLOSE).getStyleClass().add("no-skin-button");

        cboxNewEntryRole.setConverter(GameRole.STRING_CONVERTER);

        searchEntryField.textProperty().addListener(((obs, o, n) -> {
            if (n.length() > Account.MAX_NAME_LENGTH) {
                searchEntryField.setText(o);
                return;
            }
            entryQueryChanged = true;
        }));

        for (int i = 1; i < GameRole.values().length - 1; i++) {
            CheckMenuItem item = new CheckMenuItem(GameRole.getRole(i).getName());
            item.setSelected(true);
            mbFilterRole.getItems().add(item);
            cboxNewEntryRole.getItems().add(GameRole.getRole(i));
        }
        cboxNewEntryRole.getSelectionModel().selectFirst();
    }
    protected DialogPane loadDialogPane() {
        return loadDialogPane("whitelist-dialog.fxml");
    }
    public void setMetadata(GameMetadata metadata) {
        this.metadata = metadata;
        gameNameLabel.setText("Whitelist von " + metadata.getCompositeName() + " bearbeiten");

        loadWhitelist();
    }

    public void loadWhitelist() {
        jdbi.withExtension(WhitelistDao.class, dao -> dao.getWhitelist(metadata.getId(), metadata.getAccount().getName()))
                .forEach(entry -> addEntry(entry, false));
        onSearchEntries();
    }
    @FXML
    private void onAddEntry() {
        if (selectedAccount == null) return;
        vbAccounts.getChildren().remove(selectedAccountLabel);
        GameRole newRole = cboxNewEntryRole.getValue();
        SelectableWhitelistEntry entry = new SelectableWhitelistEntry(metadata.getId(), selectedAccount.getName(), newRole.getValue());
        addEntry(entry, true);

        added.put(selectedAccount.getName(), newRole);
        removed.remove(selectedAccount.getName());
        saveButton.setDisable(false);

        filterEntries(searchEntryField.getText().strip().toLowerCase());
    }
    private void addEntry(SelectableWhitelistEntry entry, boolean sortIn) {
        entry.setOnRemove(event -> onEntryRemoved(entry));
        entry.setOnChange(event -> onEntryChanged(entry));
        if (sortIn) {
            sortIntoEntries(entry);
        } else {
            allEntries.add(entry);
        }
    }
    private void sortIntoEntries(SelectableWhitelistEntry entry) {
        for (int i = 0; i < allEntries.size(); i++) {
            SelectableWhitelistEntry e = allEntries.get(i);
            if (e.getRole().compareTo(entry.getRole()) < 0 || e.getAccountName().compareTo(entry.getAccountName()) > 0) {
                allEntries.add(i, entry);
                return;
            }
        }
        allEntries.add(entry);
    }
    private void onEntryRemoved(SelectableWhitelistEntry entry) {
        allEntries.remove(entry);
        added.remove(entry.getAccountName());
        changed.remove(entry.getAccountName());
        removed.add(entry.getAccountName());
        saveButton.setDisable(false);

        vbEntries.getChildren().remove(entry);
    }
    private void onEntryChanged(SelectableWhitelistEntry entry) {
        String accountName = entry.getAccountName();
        if (added.containsKey(accountName)) return;
        changed.put(entry.getAccountName(), entry.getRole());
        saveButton.setDisable(false);
    }

    @FXML private void onSearchEntryFieldEnter() {
        bSearchEntries.requestFocus();
        bSearchEntries.fire();
    }
    @FXML private void onSearchEntries() {
        if (!entryQueryChanged) return;
        entryQueryChanged = false;

        String query = searchEntryField.getText().strip().toLowerCase();
        vbEntries.getChildren().clear();

        filterEntries(query);
    }
    private void filterEntries(String query) {
        allEntries.stream()
                .filter(entry ->
                        entry.getAccountName().toLowerCase().startsWith(query) &&
                                ((CheckMenuItem) mbFilterRole.getItems().get(entry.getRole().getValue() - 1)).isSelected()
                )
                .forEach(entry -> vbEntries.getChildren().add(entry));
    }

    private void onSaveEntries(ActionEvent event) {
        event.consume();
        saveButton.setDisable(true);

        jdbi.useExtension(WhitelistDao.class, dao -> {
            if (!removed.isEmpty()) dao.removeEntries(metadata.getId(), removed.toArray(String[]::new));
            if (!added.isEmpty()) added.forEach((name, role) -> dao.insertEntry(metadata.getId(), name, role.getValue()));
            if (!changed.isEmpty()) changed.forEach((name, role) -> dao.updateEntry(metadata.getId(), name, role.getValue()));
        });

        added.clear();
        changed.clear();
        removed.clear();
    }

    @Override
    protected void beforeSearchAccounts() {
        bAddEntry.setDisable(true);
        selectedAccount = null;
    }
    @Override
    protected List<String> getQueriedAccountNames(String query) {
        String[] addedNames = added.keySet().toArray(String[]::new);
        return jdbi.withExtension(AccountDao.class, dao ->
                (addedNames.length > 0 ? dao.getUnlistedAccountNamesByQuery(query, metadata.getId(), addedNames)
        : dao.getUnlistedAccountNamesByQuery(query, metadata.getId())));
    }
    @Override
    protected void onLabelClick(String accountName) {
        selectedAccount = loadSelectedAccount();
        bAddEntry.setDisable(selectedAccount == null);
    }
    @Override
    protected void onShowAccountInfo() {
        if (selectedAccount == null) return;
        CustomDialog.openAccountInfoAlert(selectedAccount);
    }
}