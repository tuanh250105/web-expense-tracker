package com.expensemanager.dao;

import com.expensemanager.model.Account;
import com.expensemanager.model.Category;
import com.expensemanager.model.Transaction;
import com.expensemanager.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.*;
import java.util.stream.Collectors;

public class TransactionDAO {


    private EntityManager em;
    private EntityManager em() {return JpaUtil.getEntityManager(); };



    // Account temp
    public List<Account> getAllAccountByUserId(UUID userId) {
        em = em();
        try {
            String jpql = "SELECT a FROM Account a WHERE a.user.id = :userId";
            return em.createQuery(jpql, Account.class)
                    .setParameter("userId", userId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    //Danh cho Transaction của K
    public Transaction getTransactionById(UUID transactionId) {
        em = em();
        try {
            String jpql = """
                    SELECT t FROM Transaction t 
                    JOIN FETCH t.category c 
                    JOIN FETCH t.account a 
                    WHERE t.id = :transactionId
                    """;
            return em.createQuery(jpql, Transaction.class).setParameter("transactionId", transactionId).getSingleResult();
        } finally {
            em.close();
        }
    }

    public List<Transaction> getAllTransactionsByMonthAndYear(UUID userId, LocalDateTime startOfMonth, LocalDateTime endOfMonth) {
        em = em();
        try {
            String jpql = """
                    SELECT t FROM Transaction t
                    JOIN FETCH t.category c
                    JOIN FETCH t.account a
                    WHERE a.user.id = :userId
                        AND t.transactionDate >=:startOfMonth
                        AND t.transactionDate <:endOfMonth
                    ORDER BY t.transactionDate DESC
                    """;
            return em.createQuery(jpql, Transaction.class).setParameter("userId", userId).setParameter("startOfMonth", startOfMonth).setParameter("endOfMonth", endOfMonth).getResultList();
        } finally {
            em.close();
        }
    }

    public void addIncomeTransaction(Transaction transaction) {
        em = em();
        try {
            em.getTransaction().begin();
            Account account = em.find(Account.class, transaction.getAccount().getId());
            Category category = em.find(Category.class, transaction.getCategory().getId());
            transaction.setAccount(account);
            transaction.setCategory(category);
            em.persist(transaction);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback(); throw e;
        } finally {
            em.close();
        }
    }

    public void addExpenseTransaction(Transaction transaction) {
        em = em();
        try {
            em.getTransaction().begin();
            Account account = em.find(Account.class, transaction.getAccount().getId());
            Category category = em.find(Category.class, transaction.getCategory().getId());
            transaction.setAccount(account);
            transaction.setCategory(category);
            em.persist(transaction);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();throw e;
        } finally {
            em.close();
        }
    }

    public void updateTransaction(Transaction transaction) {
        em = em();
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

    public void deleteTransaction(Transaction transaction) {
        em = em();
        try {
            em.getTransaction().begin();
            em.remove(em.merge(transaction));
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    //Filter panel
    public List<Transaction> filter(UUID userId, String fromDate, String toDate, String notes, String[] types, String categoryId) {
        em = em();
        try {
            StringBuilder jpql = new StringBuilder("""
                SELECT t FROM Transaction t
                JOIN FETCH t.category c
                JOIN FETCH t.account a
                WHERE a.user.id = :userId
            """);

            if (fromDate != null && !fromDate.isEmpty())
                jpql.append(" AND t.transactionDate >= :fromDate");
            if (toDate != null && !toDate.isEmpty()) jpql.append(" AND t.transactionDate < :toDate");
            if (notes != null && !notes.isEmpty()) jpql.append(" AND LOWER(t.note) LIKE LOWER(:notes)");
            if (types != null && types.length > 0) jpql.append(" AND LOWER(t.type) IN :types");
            if (categoryId != null && !categoryId.isEmpty()) jpql.append(" AND t.category.id = :categoryId");
            jpql.append(" ORDER BY t.transactionDate DESC");

            TypedQuery<Transaction> query = em.createQuery(jpql.toString(), Transaction.class);
            query.setParameter("userId", userId);

            if (fromDate != null && !fromDate.isEmpty())
                query.setParameter("fromDate", LocalDate.parse(fromDate).atStartOfDay());
            if (toDate != null && !toDate.isEmpty())
                query.setParameter("toDate", LocalDate.parse(toDate).plusDays(1).atStartOfDay());
            if (notes != null && !notes.isEmpty())
                query.setParameter("notes", "%" + notes + "%");
            if (types != null && types.length > 0) {
                List<String> normalized = Arrays.stream(types)
                        .filter(s -> s != null && !s.isEmpty())
                        .map(s -> s.toLowerCase(Locale.ENGLISH))
                        .collect(Collectors.toList());
                if (!normalized.isEmpty()) query.setParameter("types", normalized);
            }
            if (categoryId != null && !categoryId.isEmpty()) {
                query.setParameter("categoryId", UUID.fromString(categoryId));
            }

            return query.getResultList();
        } finally {
            em.close();
        }
    }

    //Module Budget của Nhi
    public List<Transaction> findTransactionByCategoryIdAndDate(UUID categoryId, LocalDate fromDate, LocalDate toDate) {
        em = em();
        try {
            String jpql = """
                SELECT t FROM Transaction t
                JOIN FETCH t.category c
                WHERE t.category.id = :categoryId
                AND t.transactionDate >= :fromDate
                AND t.transactionDate < :toDate
            """;
            return em.createQuery(jpql, Transaction.class)
                    .setParameter("categoryId", categoryId)
                    .setParameter("fromDate", fromDate.atStartOfDay())
                    .setParameter("toDate", toDate.plusDays(1).atStartOfDay())
                    .getResultList();
        } finally {
            em.close();
        }
    }

    //Module Schelduled của Dư
    public boolean hasTransactionNearDue(UUID categoryId, BigDecimal amount, String type, LocalDateTime start, LocalDateTime end, UUID userId) {
        em = em();
        try {
            String jpql = """
                SELECT COUNT(t) > 0 FROM Transaction t
                JOIN t.account a
                WHERE t.category.id = :categoryId
                AND t.amount = :amount
                AND LOWER(t.type) = LOWER(:type)
                AND t.transactionDate BETWEEN :start AND :end
                AND a.user.id = :userId
            """;
            return (boolean) em.createQuery(jpql)
                    .setParameter("categoryId", categoryId)
                    .setParameter("amount", amount)
                    .setParameter("type", type)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .setParameter("userId", userId)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    //THỐNG KÊ
    //My
    public Map<String, Double> calculateSummary(List<Transaction> list) {
        double income = list.stream()
                .filter(t -> "income".equalsIgnoreCase(t.getType()))
                .mapToDouble(t -> t.getAmount() != null ? t.getAmount().doubleValue() : 0.0)
                .sum();
        double expense = list.stream()
                .filter(t -> "expense".equalsIgnoreCase(t.getType()))
                .mapToDouble(t -> t.getAmount() != null ? t.getAmount().doubleValue() : 0.0)
                .sum();
        Map<String, Double> summary = new LinkedHashMap<>();
        summary.put("income", income);
        summary.put("expense", expense);
        summary.put("balance", income - expense);
        return summary;
    }

    public List<Map<String, Object>> groupTransactionsByTime(List<Transaction> list, String group) {
        Map<String, Double> grouped = new TreeMap<>();
        for (Transaction t : list) {
            if (t.getTransactionDate() == null) continue;
            String key = switch (group.toLowerCase()) {
                case "month" -> String.format("%d-%02d", t.getTransactionDate().getYear(), t.getTransactionDate().getMonthValue());
                case "year" -> String.valueOf(t.getTransactionDate().getYear());
                default -> t.getTransactionDate().toLocalDate().toString();
            };
            double amt = (t.getAmount() != null ? t.getAmount().doubleValue() : 0.0);
            if ("expense".equalsIgnoreCase(t.getType())) amt = -amt;
            grouped.merge(key, amt, Double::sum);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Double> e : grouped.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("label", e.getKey());
            item.put("value", e.getValue());
            result.add(item);
        }
        return result;
    }

    public List<Map<String, Object>> groupTransactionsByCategory(List<Transaction> list, int topN) {
        Map<String, Double> grouped = list.stream()
                .collect(Collectors.groupingBy(
                        t -> (t.getCategory() != null && t.getCategory().getName() != null)
                                ? t.getCategory().getName()
                                : "Không xác định",
                        Collectors.summingDouble(t -> t.getAmount() != null ? t.getAmount().doubleValue() : 0.0)
                ));

        return grouped.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topN)
                .map(e -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("categoryName", e.getKey());
                    item.put("total", e.getValue());
                    return item;
                })
                .collect(Collectors.toList());
    }

    public long countByCategoryId(UUID categoryId) {
        em = em();
        try {
            String jpql = "SELECT COUNT(t) FROM Transaction t WHERE t.category.id = :categoryId";
            return em.createQuery(jpql, Long.class)
                    .setParameter("categoryId", categoryId)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }
}
