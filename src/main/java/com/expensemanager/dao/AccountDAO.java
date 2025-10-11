package com.expensemanager.dao;

import com.expensemanager.model.Account;
import com.expensemanager.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import java.util.UUID;

public class AccountDAO {

    private static final EntityManagerFactory emf = JpaUtil.getEntityManagerFactory();

    public List<Account> getAllAccounts() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT a FROM Account a", Account.class).getResultList();
        } finally {
            em.close();
        }
    }

    public Account findById(UUID id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Account.class, id);
        } finally {
            em.close();
        }
    }
}
