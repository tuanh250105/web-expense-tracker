package com.expensemanager.util;

import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

<<<<<<< HEAD
public class JPAUtil {
  private static final EntityManagerFactory emf;
  static {
    Map<String, Object> props = new HashMap<>();
    String url = System.getenv("DB_URL");
    String user = System.getenv("DB_USER");
    String pass = System.getenv("DB_PASS");
    if (url != null) props.put("jakarta.persistence.jdbc.url", url);
    if (user != null) props.put("jakarta.persistence.jdbc.user", user);
    if (pass != null) props.put("jakarta.persistence.jdbc.password", pass);
    emf = Persistence.createEntityManagerFactory("expensePU", props);
  }
  public static EntityManager em() { return emf.createEntityManager(); }

    public static EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
=======
public class JpaUtil {
    private static EntityManagerFactory entityManagerFactory;
    
    static {
        try {
            // Sử dụng persistence unit name đúng từ persistence.xml
            entityManagerFactory = Persistence.createEntityManagerFactory("BudgetBuddyUnit");
            System.out.println("✅ JpaUtil: EntityManagerFactory initialized successfully!");
        } catch (Exception e) {
            System.err.println("❌ JpaUtil: Initial EntityManagerFactory creation failed: " + e);
            throw new ExceptionInInitializerError(e);
        }
    }
    
    public static EntityManager getEntityManager() {
        return entityManagerFactory.createEntityManager();
    }
    
    public static EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }
    
    public static void close() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
            System.out.println("✅ JpaUtil: EntityManagerFactory closed!");
>>>>>>> feature/bank
        }
    }

}