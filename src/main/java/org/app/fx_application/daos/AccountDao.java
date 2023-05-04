package org.app.fx_application.daos;

import org.app.game_classes.Account;

import java.util.List;
import java.util.UUID;

import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.*;
import org.jdbi.v3.sqlobject.config.*;

public interface AccountDao {
    @RegisterConstructorMapper(Account.class)
    @SqlQuery("SELECT * FROM account WHERE name = :name")
    Account getByName(String name);

    @RegisterConstructorMapper(Account.class)
    @SqlQuery("SELECT * FROM account WHERE email = :email")
    Account getByEmail(String email);

    @SqlQuery("SELECT name FROM account WHERE last_login < DATEADD(month, -:monthsSinceLastLogin, GETDATE())")
    List<String> getAbandonedAccountNames(int monthsSinceLastLogin);

    @SqlQuery("SELECT name FROM account WHERE name LIKE :query + '%' ORDER BY name")
    List<String> getAccountNamesByQuery(String query);

    @SqlQuery("SELECT TOP 100 name FROM account " +
            "WHERE name LIKE :query + '%' AND " +
            "NOT EXISTS(SELECT 1 FROM game_whitelist WHERE game_id = :gameId AND account_name = account.name) " +
            "AND name NOT IN (<unsavedListedNames>) ORDER BY NAME")
    List<String> getUnlistedAccountNamesByQuery(String query, UUID gameId, @BindList(onEmpty = BindList.EmptyHandling.NULL_STRING) String... unsavedListedNames);

    @SqlQuery("SELECT TOP 100 name FROM account " +
            "WHERE name LIKE :query + '%' AND " +
            "NOT EXISTS(SELECT 1 FROM game_whitelist WHERE game_id = :gameId AND account_name = account.name) ORDER BY NAME")
    List<String> getUnlistedAccountNamesByQuery(String query, UUID gameId);

    @SqlQuery("IF EXISTS(SELECT 1 FROM account WHERE name = :name) SELECT 1 ELSE SELECT 0")
    boolean existsByName(String name);

    @SqlQuery("IF EXISTS(SELECT 1 FROM account WHERE email = :email) SELECT 1 ELSE SELECT 0")
    boolean existsByEmail(String email);

    @SqlUpdate("INSERT INTO account (name, pw_hash, email) VALUES (:getName, :getPasswordHash, :getEmail)")
    void saveNewAccount(@BindMethods Account account);

    @SqlUpdate("UPDATE account SET name = :getName, description = :getDescription, email = :getEmail, pw_hash = :getPasswordHash, " +
            "allow_passive_game_joining = :isAllowPassiveGameJoining WHERE name = :oldName")
    void updateAccount(String oldName, @BindMethods Account account);

    @SqlUpdate("UPDATE account SET last_login = GETDATE() WHERE name = :name")
    void updateLastLogin(String name);

    @SqlUpdate("DELETE FROM account WHERE name = :name")
    void deleteAccount(String name);

    @SqlUpdate("DELETE FROM account WHERE name IN (<accountNames>)")
    void deleteAccounts(@BindList(onEmpty = BindList.EmptyHandling.NULL_STRING) List<String> accountNames);
}