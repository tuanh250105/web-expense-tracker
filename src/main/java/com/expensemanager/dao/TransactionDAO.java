package com.expensemanager.dao;

import com.expensemanager.model.Account;
import com.expensemanager.model.Category;
import com.expensemanager.model.Transaction;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class TransactionDAO {

    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("BudgetBuddyUnit");

    public Account findAccountById(UUID id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Account.class, id);
        } finally {
            em.close();
        }
    }

    public List<Account> getAllAccountByUserId(UUID userId) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT a FROM Account a WHERE a.user.id = :userId";
            return em.createQuery(jpql, Account.class)
                    .setParameter("userId", userId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Category findCategoryById(UUID id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Category.class, id);
        } finally {
            em.close();
        }
    }

    public List<Category> findAllCategoryOfUser(UUID userId){
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT c FROM Category c WHERE c.user.id = :userId";
            return em.createQuery(jpql, Category.class)
                    .setParameter("userId", userId)
                    .getResultList();
        }
        finally {
            em.close();
        }
    }

    public Transaction getTransactionById(UUID transactionId) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT t FROM Transaction t " +
                    "JOIN FETCH t.category c " +
                    "JOIN FETCH t.account a " +
                    "WHERE t.id = :transactionId ";
            return em.createQuery(jpql, Transaction.class)
                    .setParameter("transactionId", transactionId)
                    .getSingleResult();
        }
        finally {
            em.close();
        }
    }

    public List<Transaction> getAllTransactionsByMonthAndYear(UUID userId, LocalDateTime startOfMonth, LocalDateTime endOfMonth) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT t FROM Transaction t " +
                    "JOIN FETCH t.category c " +
                    "JOIN FETCH t.account a " +
                    "WHERE a.user.id = :userId " +
                    "AND t.transactionDate >= :startOfMonth " +
                    "AND t.transactionDate < :endOfMonth " +
                    "ORDER BY t.transactionDate DESC";

            return em.createQuery(jpql, Transaction.class)
                    .setParameter("userId", userId)
                    .setParameter("startOfMonth", startOfMonth)
                    .setParameter("endOfMonth", endOfMonth)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public void addIncomeTransaction(Transaction transaction) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            // Nạp Account và Category thực từ DB để đảm bảo hợp lệ
            Account account = em.find(Account.class, transaction.getAccount().getId());
            Category category = em.find(Category.class, transaction.getCategory().getId());

            transaction.setAccount(account);
            transaction.setCategory(category);

            em.persist(transaction);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void addExpenseTransaction(Transaction transaction) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            // Nạp Account và Category thực từ DB để đảm bảo hợp lệ
            Account account = em.find(Account.class, transaction.getAccount().getId());
            Category category = em.find(Category.class, transaction.getCategory().getId());

            transaction.setAccount(account);
            transaction.setCategory(category);

            em.persist(transaction);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void updateTransaction(Transaction transaction){
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(transaction);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void deleteTransaction(Transaction transaction){
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.remove(em.merge(transaction));
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    public List<Transaction> filter(UUID userId, String fromDate, String toDate, String notes, String type) {
        EntityManager em = emf.createEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT t FROM Transaction t " +
                            "JOIN FETCH t.category c " +
                            "JOIN FETCH t.account a " +
                            "WHERE a.user.id = :userId"
            );

            if (fromDate != null) {
                jpql.append(" AND t.transactionDate >= :fromDate");
            }
            if (toDate != null) {
                jpql.append(" AND t.transactionDate < :toDate");
            }
            if (notes != null && !notes.isEmpty()) {
                jpql.append(" AND LOWER(t.note) LIKE LOWER(:notes)");
            }
            if (type != null && !type.isEmpty()) {
                jpql.append(" AND t.type LIKE :type");
            }

            jpql.append(" ORDER BY t.transactionDate DESC");

            TypedQuery<Transaction> query = em.createQuery(jpql.toString(), Transaction.class);
            query.setParameter("userId", userId);

            if (fromDate != null) {
                query.setParameter("fromDate", LocalDate.parse(fromDate).atStartOfDay());
            }
            if (toDate != null) {
                query.setParameter("toDate", LocalDate.parse(toDate).plusDays(1).atStartOfDay());
            }
            if (notes != null && !notes.isEmpty()) {
                query.setParameter("notes", "%" + notes + "%");
            }
            if (type != null && !type.isEmpty()) {
                query.setParameter("type", "%" + type + "%");
            }

            return query.getResultList();
        } finally {
            em.close();
        }
    }

}