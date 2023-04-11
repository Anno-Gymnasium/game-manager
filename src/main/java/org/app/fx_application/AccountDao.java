package org.app.fx_application;

import org.app.game_classes.Account;

import java.util.List;

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

    @SqlQuery("IF EXISTS(SELECT 1 FROM account WHERE name = :name) SELECT 1 ELSE SELECT 0")
    boolean existsByName(String name);

    @SqlQuery("IF EXISTS(SELECT 1 FROM account WHERE email = :email) SELECT 1 ELSE SELECT 0")
    boolean existsByEmail(String email);

    @SqlUpdate("INSERT INTO account (name, pw_hash, email) VALUES (:getName, :getPasswordHash, :getEmail)")
    void saveNewAccount(@BindMethods Account account);

    @SqlUpdate("UPDATE account SET name = :getName, description = :getDescription, email = :getEmail, pw_hash = :getPasswordHash," +
            "allow_passive_game_joining = :isAllowPassiveGameJoining WHERE name = :oldName")
    void updateAccount(String oldName, @BindMethods Account account);

    @SqlUpdate("UPDATE account SET last_login = GETDATE() WHERE name = :name")
    void updateLastLogin(String name);

    @SqlUpdate("DELETE FROM account WHERE name = :name")
    void deleteAccount(String name);
}