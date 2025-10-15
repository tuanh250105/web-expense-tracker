package com.expensemanager.dao;

import com.expensemanager.model.Transaction;
import com.expensemanager.util.JpaUtil;  // ✅ thêm import này

import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ImportExportDAO {

    // ✅ Dùng EntityManagerFactory từ JpaUtil (đã cấu hình DB_URL, DB_USER, DB_PASS)
    private  EntityManager em;
    private EntityManager em() {return JpaUtil.getEntityManager(); };
    /**
     * Lưu danh sách Transaction vào database
     */
    public void saveTransactions(List<Transaction> transactions) {
        em = em();
        em.getTransaction().begin();
        try {
            for (Transaction t : transactions) {
                // Merge các đối tượng detached (Account, Category)
                if (t.getAccount() != null && t.getAccount().getId() != null) {
                    t.setAccount(em.merge(t.getAccount()));
                }
                if (t.getCategory() != null && t.getCategory().getId() != null) {
                    t.setCategory(em.merge(t.getCategory()));
                }
                em.persist(t);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lưu dữ liệu: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Lấy Transaction theo account và khoảng thời gian
     */
    public List<Transaction> getTransactionsByAccountAndDate(UUID accountId, LocalDate startDate, LocalDate endDate) {
        em = em();
        try {
            String jpql = "SELECT t FROM Transaction t " +
                    "WHERE t.account.id = :accId " +
                    "AND t.transactionDate BETWEEN :start AND :end " +
                    "ORDER BY t.transactionDate DESC";
            var query = em.createQuery(jpql, Transaction.class);
            query.setParameter("accId", accountId);
            query.setParameter("start", startDate.atStartOfDay());
            query.setParameter("end", endDate.atTime(23, 59, 59));
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy toàn bộ Transaction từ database
     */
    public List<Transaction> getAllTransactions() {
        em = em();
        try {
            return em.createQuery("SELECT t FROM Transaction t", Transaction.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
