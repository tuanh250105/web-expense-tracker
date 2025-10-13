package com.expensemanager.util;

import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaUtil {
  private static EntityManagerFactory emf;
  static {
    Map<String, Object> props = new HashMap<>();
    String url = System.getenv("DB_URL");
    String user = System.getenv("DB_USER");
    String pass = System.getenv("DB_PASS");
    if (url != null) props.put("jakarta.persistence.jdbc.url", url);
    if (user != null) props.put("jakarta.persistence.jdbc.user", user);
    if (pass != null) props.put("jakarta.persistence.jdbc.password", pass);

    // Chọn persistence file theo user.name
    String username = System.getProperty("user.name", "");
    String unitName = "default"; // our persistence-unit name
    try {
      if ("tuanh250105".equalsIgnoreCase(username)) {
        // Load persistence-dev.xml by using same unit name and classpath override
        // JPA sẽ tự động phát hiện file META-INF/persistence.xml; persistence-dev.xml tồn tại để override props qua code
      }
      emf = Persistence.createEntityManagerFactory(unitName, props);
    } catch (RuntimeException ex) {
      // Fallback an toàn
      emf = Persistence.createEntityManagerFactory(unitName);
    }
  }
  public static EntityManager em() { return emf.createEntityManager(); }

    public static EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
  }

    public static EntityManager getEntityManager() {
        if (emf == null || !emf.isOpen()) {
            emf = Persistence.createEntityManagerFactory("default");
        }
        return emf.createEntityManager();
    }

    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
            System.out.println("✅ JpaUtil: EntityManagerFactory closed!");
        }
    }
}