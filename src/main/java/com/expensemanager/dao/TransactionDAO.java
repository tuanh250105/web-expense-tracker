//package com.expensemanager.dao;
//
//import com.expensemanager.entity.Transaction;
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.PersistenceContext;
//import jakarta.transaction.Transactional;
//import org.springframework.stereotype.Repository;
//import java.util.List;
//import java.util.UUID;
//
//@Repository
//@Transactional
//public class TransactionDAO {
//
//    @PersistenceContext
//    private EntityManager em;
//
//    public List<Transaction> getAllByUser(UUID userId) {
//        return em.createQuery("SELECT t FROM Transaction t WHERE t.userId = :userId", Transaction.class)
//                .setParameter("userId", userId)
//                .getResultList();
//    }
//}
