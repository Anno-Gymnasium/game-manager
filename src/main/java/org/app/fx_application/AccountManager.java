package org.app.fx_application;

import org.app.game_classes.Account;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.hibernate.Session;

public class AccountManager {
    private EntityManager entityManager;

    public AccountManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Account getByName(String username) {
        return entityManager.find(Account.class, username);
    }
    public Account getByEmail(String email) {
        Session session = entityManager.unwrap(Session.class);
        return session.createQuery("from Account where email = :email", Account.class)
                .setParameter("email", email)
                .uniqueResult();

    }
    @Transactional
    public void saveNewAccount(Account account) {
        entityManager.persist(account);
    }
    @Transactional
    public void updateAccount(Account account) {
        entityManager.merge(account);
    }
    @Transactional
    public void deleteAccount(Account account) {
        entityManager.remove(account);
    }
}