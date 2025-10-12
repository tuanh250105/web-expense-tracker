package com.expensemanager.dao;

import java.util.List;
import java.util.UUID;

import com.expensemanager.model.Account;
import com.expensemanager.util.JpaUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

/**
 * AccountDAO - Data Access Object for Account entity
 */
public class AccountDAO {
    
    public void save(Account account) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            System.out.println("🔵 AccountDAO.save() starting transaction");
            em.getTransaction().begin();
            
            // If account has a user with ID but not managed, fetch it from database
            if (account.getUser() != null && account.getUser().getId() != null) {
                System.out.println("👤 Account has user with ID: " + account.getUser().getId());
                
                // Check if user is detached (has ID but not managed)
                if (!em.contains(account.getUser())) {
                    System.out.println("⚠️ User is detached, fetching from database...");
                    
                    // Fetch the managed user from database
                    com.expensemanager.model.User managedUser = em.find(com.expensemanager.model.User.class, account.getUser().getId());
                    
                    if (managedUser != null) {
                        System.out.println("✅ Found managed user with ID: " + managedUser.getId());
                        account.setUser(managedUser);
                    } else {
                        System.err.println("❌ User not found in database with ID: " + account.getUser().getId());
                        throw new IllegalArgumentException("User not found with ID: " + account.getUser().getId());
                    }
                }
            } else {
                System.out.println("⚠️ Account has no user set!");
            }
            
            if (account.getId() == null) {
                System.out.println("💾 Persisting new account: " + account.getName());
                em.persist(account);
            } else {
                System.out.println("🔄 Merging existing account: " + account.getName());
                em.merge(account);
            }
            
            em.getTransaction().commit();
            System.out.println("✅ Transaction committed successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Error in AccountDAO.save(): " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            
            if (em.getTransaction().isActive()) {
                System.err.println("🔴 Rolling back transaction");
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
    
    public Account findById(UUID id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(Account.class, id);
        } finally {
            em.close();
        }
    }
    
    public List<Account> findAllByUser(UUID userId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT a FROM Account a WHERE a.user.id = :userId ORDER BY a.name ASC";
            TypedQuery<Account> query = em.createQuery(jpql, Account.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Account> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT a FROM Account a ORDER BY a.name ASC";
            return em.createQuery(jpql, Account.class).getResultList();
        } finally {
            em.close();
        }
    }
    
    public void update(Account account) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(account);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
    
    public void delete(UUID id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Account account = em.find(Account.class, id);
            if (account != null) {
                em.remove(account);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
