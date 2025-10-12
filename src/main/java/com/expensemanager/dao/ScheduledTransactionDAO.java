package com.expensemanager.dao;

import com.expensemanager.model.Account;
import com.expensemanager.model.ScheduledTransaction;
import com.expensemanager.model.User;
import com.expensemanager.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScheduledTransactionDAO {

    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("default");
    public List<ScheduledTransaction> getAll() {
        return getFiltered(null, null, null, null, null, null, null);
    }

    public List<ScheduledTransaction> getFiltered(String categoryNameFilter, String account,
                                                  String from, String to, String note,
                                                  String[] types, UUID userId) {
        EntityManager em = emf.createEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT s FROM ScheduledTransaction s " +
                            "JOIN s.category c " +
                            "JOIN s.account a " +
                            "WHERE 1=1 "
            );

            // Lọc theo user
            if (userId != null) {
                jpql.append("AND a.user.id = :userId ");
            }
            if (categoryNameFilter != null && !categoryNameFilter.isEmpty()) {
                jpql.append("AND LOWER(c.name) LIKE LOWER(:categoryName) ");
            }
            if (account != null && !account.isEmpty()) {
                jpql.append("AND LOWER(a.name) LIKE LOWER(:account) ");
            }
            if (from != null && !from.isEmpty()) {
                jpql.append("AND s.nextRun >= :fromTs ");
            }
            if (to != null && !to.isEmpty()) {
                jpql.append("AND s.nextRun <= :toTs ");
            }
            if (note != null && !note.isEmpty()) {
                jpql.append("AND LOWER(s.note) LIKE LOWER(:note) ");
            }
            if (types != null && types.length > 0) {
                jpql.append("AND LOWER(s.type) IN (");
                for (int i = 0; i < types.length; i++) {
                    if (i > 0) jpql.append(",");
                    jpql.append(":type" + i);
                }
                jpql.append(") ");
            }
            jpql.append("ORDER BY s.nextRun ASC");

            Query query = em.createQuery(jpql.toString());

            // Gán tham số
            if (userId != null) query.setParameter("userId", userId);
            if (categoryNameFilter != null && !categoryNameFilter.isEmpty())
                query.setParameter("categoryName", "%" + categoryNameFilter.toLowerCase() + "%");
            if (account != null && !account.isEmpty())
                query.setParameter("account", "%" + account.toLowerCase() + "%");
            if (from != null && !from.isEmpty())
                query.setParameter("fromTs", Timestamp.valueOf(from + " 00:00:00"));
            if (to != null && !to.isEmpty())
                query.setParameter("toTs", Timestamp.valueOf(to + " 23:59:59"));
            if (note != null && !note.isEmpty())
                query.setParameter("note", "%" + note.toLowerCase() + "%");
            if (types != null && types.length > 0) {
                for (int i = 0; i < types.length; i++) {
                    query.setParameter("type" + i, types[i].toLowerCase());
                }
            }

            @SuppressWarnings("unchecked")
            List<ScheduledTransaction> list = query.getResultList();
            for (ScheduledTransaction st : list) {
                if (st.getCategory() != null)
                    st.setCategoryName(st.getCategory().getName());
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public List<Account> getAccountsByUserId(UUID userId) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT a FROM Account a WHERE a.user.id = :userId";
            Query query = em.createQuery(jpql, Account.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Account findAccountById(UUID id, UUID userId) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT a FROM Account a WHERE a.id = :id AND a.user.id = :userId";
            Query query = em.createQuery(jpql, Account.class);
            query.setParameter("id", id);
            query.setParameter("userId", userId);
            return (Account) query.getSingleResult();
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

    public void add(ScheduledTransaction t) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(t);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Lỗi khi thêm ScheduledTransaction", e);
        } finally {
            em.close();
        }
    }

    public void update(ScheduledTransaction t) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(t);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Lỗi khi cập nhật ScheduledTransaction", e);
        } finally {
            em.close();
        }
    }

    public void delete(UUID id, UUID userId) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            String jpql = "SELECT s FROM ScheduledTransaction s JOIN s.account a WHERE s.id = :id AND a.user.id = :userId";
            Query selectQuery = em.createQuery(jpql, ScheduledTransaction.class);
            selectQuery.setParameter("id", id);
            selectQuery.setParameter("userId", userId);
            ScheduledTransaction t = (ScheduledTransaction) selectQuery.getSingleResult();
            em.remove(t);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Lỗi khi xóa ScheduledTransaction", e);
        } finally {
            em.close();
        }
    }

    public List<ScheduledTransaction> getDueTransactions() {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            String jpql = "SELECT s FROM ScheduledTransaction s " +
                    "LEFT JOIN FETCH s.category " +
                    "LEFT JOIN FETCH s.account " +
                    "WHERE s.active = true AND s.nextRun <= CURRENT_TIMESTAMP ORDER BY s.nextRun ASC";
            Query query = em.createQuery(jpql);
            List<ScheduledTransaction> list = query.getResultList();
            for (ScheduledTransaction st : list) {
                if (st.getCategory() != null)
                    st.setCategoryName(st.getCategory().getName());
            }
            return list;
        } finally {
            em.close();
        }
    }

    public ScheduledTransaction getById(UUID id, UUID userId) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            String jpql = "SELECT s FROM ScheduledTransaction s " +
                    "LEFT JOIN FETCH s.category " +
                    "LEFT JOIN FETCH s.account a " +
                    "WHERE s.id = :id AND a.user.id = :userId";
            Query query = em.createQuery(jpql);
            query.setParameter("id", id);
            query.setParameter("userId", userId);
            ScheduledTransaction st = (ScheduledTransaction) query.getSingleResult();
            if (st != null && st.getCategory() != null)
                st.setCategoryName(st.getCategory().getName());
            return st;
        } finally {
            em.close();
        }
    }

    public List<ScheduledTransaction> getUpcomingTransactions(int daysAhead) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            // Tính toán end timestamp: now + daysAhead ngày
            Timestamp now = new Timestamp(System.currentTimeMillis());
            Timestamp end = new Timestamp(now.getTime() + ((long) daysAhead * 86400000L));

            String jpql = "SELECT s FROM ScheduledTransaction s " +
                    "LEFT JOIN FETCH s.category " +
                    "LEFT JOIN FETCH s.account a " +
                    "LEFT JOIN FETCH a.user " +
                    "WHERE s.active = true " +
                    "AND s.nextRun > CURRENT_TIMESTAMP " +
                    "AND s.nextRun <= :end " +
                    "ORDER BY s.nextRun ASC";

            Query query = em.createQuery(jpql);
            query.setParameter("end", end);

            @SuppressWarnings("unchecked")
            List<ScheduledTransaction> list = query.getResultList();

            for (ScheduledTransaction st : list) {
                if (st.getCategory() != null) {
                    st.setCategoryName(st.getCategory().getName());
                }
            }

            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }


    public boolean hasTransactionNearDue(UUID categoryId, BigDecimal amount, String type,
                                         Timestamp dueDate, int daysBefore, UUID userId) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            LocalDateTime due = dueDate.toLocalDateTime();
            LocalDateTime start = due.minusDays(daysBefore);
            LocalDateTime end = due;
            String jpql = "SELECT COUNT(t) > 0 FROM Transaction t " +
                    "JOIN t.account a " +
                    "WHERE t.category.id = :categoryId " +
                    "AND t.amount = :amount " +
                    "AND LOWER(t.type) = LOWER(:type) " +
                    "AND t.transactionDate BETWEEN :start AND :end " +
                    "AND a.user.id = :userId";
            Query query = em.createQuery(jpql);
            query.setParameter("categoryId", categoryId);
            query.setParameter("amount", amount);
            query.setParameter("type", type);
            query.setParameter("start", start);
            query.setParameter("end", end);
            query.setParameter("userId", userId);
            return (boolean) query.getSingleResult();
        } finally {
            em.close();
        }
    }

    public List<Category> getByType(String type) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            Query query = em.createQuery("SELECT c FROM Category c WHERE c.type = :type ORDER BY c.name");
            query.setParameter("type", type);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Category> getAllCategories() {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            Query query = em.createQuery("SELECT c FROM Category c ORDER BY c.name");
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public String getUserEmailByAccount(UUID accountId) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            String jpql = "SELECT u.email FROM Account a JOIN a.user u WHERE a.id = :accountId";
            Query query = em.createQuery(jpql);
            query.setParameter("accountId", accountId);
            return (String) query.getSingleResult();
        } finally {
            em.close();
        }
    }
}
