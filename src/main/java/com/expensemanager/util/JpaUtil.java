package com.expensemanager.util;
<<<<<<< HEAD

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaUtil {
    private static final EntityManagerFactory emf;

    static {
        emf = Persistence.createEntityManagerFactory("BugetBuddyUnit");  // Tên PU từ persistence.xml
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
=======
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

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
>>>>>>> 14ad5cc8000a1004f85b9762e0f1c1397356e0bb
}