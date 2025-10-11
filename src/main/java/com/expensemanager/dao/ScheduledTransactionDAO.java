package com.expensemanager.dao;

import com.expensemanager.model.Account;
import com.expensemanager.model.Category;
import com.expensemanager.model.ScheduledTransaction;
import com.expensemanager.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScheduledTransactionDAO {
    public List<ScheduledTransaction> getAll() {
        return getFiltered(null, null, null, null, null, null, null);
    }

    public List<ScheduledTransaction> getFiltered(String categoryNameFilter, String account, String from, String to, String note, String[] types, UUID userId) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT s FROM ScheduledTransaction s " +
                            "JOIN s.category c " +
                            "JOIN s.account a " +
                            "WHERE 1=1 "
            );

            // Filter userId (bắt buộc nếu có)
            if (userId != null) {
                jpql.append("AND a.userId = :userId ");
            }

            // Filter category name
            if (categoryNameFilter != null && !categoryNameFilter.isEmpty()) {
                jpql.append("AND LOWER(c.name) LIKE LOWER(:categoryName) ");
            }

            // Filter account name
            if (account != null && !account.isEmpty()) {
                jpql.append("AND LOWER(a.name) LIKE LOWER(:account) ");
            }

            // Filter from date
            if (from != null && !from.isEmpty()) {
                try {
                    Timestamp fromTs = Timestamp.valueOf(from + " 00:00:00");
                    jpql.append("AND s.nextRun >= :fromTs ");
                } catch (IllegalArgumentException e) {
                    System.err.println("Lỗi parse from date: " + from + " - Bỏ qua filter");
                }
            }

            // Filter to date
            if (to != null && !to.isEmpty()) {
                try {
                    Timestamp toTs = Timestamp.valueOf(to + " 23:59:59");
                    jpql.append("AND s.nextRun <= :toTs ");
                } catch (IllegalArgumentException e) {
                    System.err.println("Lỗi parse to date: " + to + " - Bỏ qua filter");
                }
            }

            // Filter note
            if (note != null && !note.isEmpty()) {
                jpql.append("AND LOWER(s.note) LIKE LOWER(:note) ");
            }

            // Filter types (IN clause)
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

            // Set parameters
            if (userId != null) {
                query.setParameter("userId", userId);
            }
            if (categoryNameFilter != null && !categoryNameFilter.isEmpty()) {
                query.setParameter("categoryName", "%" + categoryNameFilter.toLowerCase() + "%");
            }
            if (account != null && !account.isEmpty()) {
                query.setParameter("account", "%" + account.toLowerCase() + "%");
            }
            if (from != null && !from.isEmpty()) {
                Timestamp fromTs = Timestamp.valueOf(from + " 00:00:00");
                query.setParameter("fromTs", fromTs);
            }
            if (to != null && !to.isEmpty()) {
                Timestamp toTs = Timestamp.valueOf(to + " 23:59:59");
                query.setParameter("toTs", toTs);
            }
            if (note != null && !note.isEmpty()) {
                query.setParameter("note", "%" + note.toLowerCase() + "%");
            }
            if (types != null && types.length > 0) {
                for (int i = 0; i < types.length; i++) {
                    query.setParameter("type" + i, types[i].toLowerCase());
                }
            }

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

// ... (giữ nguyên các method khác, chỉ sửa findAccountByName)

    public Account findAccountByName(String name, UUID userId) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            System.out.println("🔍 Tìm Account: name='" + name + "' (LOWER), userId=" + userId);
            String jpql = "SELECT a FROM Account a WHERE LOWER(a.name) = LOWER(:name) AND a.userId = :userId";
            Query query = em.createQuery(jpql, Account.class);
            query.setParameter("name", name.toLowerCase());
            query.setParameter("userId", userId);
            Account acc = (Account) query.getSingleResult();
            if (acc != null) {
                System.out.println("✅ Tìm thấy Account: ID=" + acc.getId() + ", name='" + acc.getName() + "'");
            } else {
                System.out.println("❌ Không tìm thấy Account với name='" + name + "'. Danh sách account có: Ngân hàng, Thẻ tín dụng, Ví");
            }
            return acc;
        } catch (Exception e) {
            System.out.println("Lỗi query Account: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }

    public Category findCategoryById(Integer id) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            System.out.println("🔍 Tìm Category ID=" + id);
            String jpql = "SELECT c FROM Category c WHERE c.id = :id";
            Query query = em.createQuery(jpql, Category.class);
            query.setParameter("id", id);
            Category cat = (Category) query.getSingleResult();
            if (cat != null) {
                System.out.println("✅ Tìm thấy Category: ID=" + cat.getId() + ", name='" + cat.getName() + "'");
            } else {
                System.out.println("❌ Không tìm thấy Category ID=" + id);
            }
            return cat;
        } catch (Exception e) {
            System.out.println("Lỗi query Category: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }

    public void add(ScheduledTransaction t) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            System.out.println("🚀 Bắt đầu persist ScheduledTransaction:");
            System.out.println("  - Type: " + t.getType());
            System.out.println("  - Amount: " + t.getAmount());
            System.out.println("  - Account ID: " + (t.getAccount() != null ? t.getAccount().getId() : "NULL - LỖI!"));
            System.out.println("  - Category ID: " + (t.getCategory() != null ? t.getCategory().getId() : "NULL - LỖI!"));
            System.out.println("  - NextRun: " + t.getNextRun());
            System.out.println("  - Note: " + t.getNote());

            em.getTransaction().begin();
            em.persist(t);
            em.flush();  // Force INSERT để thấy SQL log
            em.getTransaction().commit();
            System.out.println("✅ Commit thành công! ID mới: " + t.getId());
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
                System.out.println("❌ Rollback transaction do lỗi: " + e.getMessage());
            }
            e.printStackTrace();
            throw new RuntimeException("Lỗi thêm ScheduledTransaction: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public void delete(UUID id, UUID userId) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            String jpql = "SELECT s FROM ScheduledTransaction s JOIN s.account a WHERE s.id = :id AND a.userId = :userId";
            Query selectQuery = em.createQuery(jpql, ScheduledTransaction.class);
            selectQuery.setParameter("id", id);
            selectQuery.setParameter("userId", userId);
            ScheduledTransaction t = (ScheduledTransaction) selectQuery.getSingleResult();
            em.remove(t);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
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

    public ScheduledTransaction getById(UUID id, UUID userId) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            String jpql = "SELECT s FROM ScheduledTransaction s " +
                    "LEFT JOIN FETCH s.category " +
                    "LEFT JOIN FETCH s.account a " +
                    "WHERE s.id = :id AND a.userId = :userId";
            Query query = em.createQuery(jpql);
            query.setParameter("id", id);
            query.setParameter("userId", userId);
            ScheduledTransaction st = (ScheduledTransaction) query.getSingleResult();
            if (st != null && st.getCategory() != null) {
                st.setCategoryName(st.getCategory().getName());
            }
            return st;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    // Method mới: Lấy các giao dịch sắp due trong vòng daysAhead ngày tới
    public List<ScheduledTransaction> getUpcomingTransactions(int daysAhead) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            // Tính toán end timestamp: now + daysAhead ngày (86400000 ms = 1 ngày)
            Timestamp now = new Timestamp(System.currentTimeMillis());
            Timestamp end = new Timestamp(now.getTime() + ((long) daysAhead * 86400000L));

            String jpql = "SELECT s FROM ScheduledTransaction s " +
                    "LEFT JOIN FETCH s.category " +
                    "LEFT JOIN FETCH s.account " +
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
}