//package com.expensemanager.dao;
//
//import java.util.List;
//import java.util.UUID;
//
//import com.expensemanager.model.Event;
//
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.EntityManagerFactory;
//import jakarta.persistence.Persistence;
//import jakarta.persistence.TypedQuery;
//
//public class EventDAO {
//    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("default");
//
//    public Event findById(UUID id) {
//        EntityManager em = emf.createEntityManager();
//        try {
//            return em.find(Event.class, id);
//        } finally {
//            em.close();
//        }
//    }
//
//    public List<Event> findByUserId(UUID userId) {
//        EntityManager em = emf.createEntityManager();
//        try {
//            TypedQuery<Event> query = em.createQuery("SELECT e FROM Event e WHERE e.userId = :userId", Event.class);
//            query.setParameter("userId", userId);
//            return query.getResultList();
//        } finally {
//            em.close();
//        }
//    }
//
//    public List<Event> findByUserIdAndDateRange(UUID userId, java.time.LocalDate startDate, java.time.LocalDate endDate) {
//        EntityManager em = emf.createEntityManager();
//        try {
//            TypedQuery<Event> query = em.createQuery(
//                "SELECT e FROM Event e WHERE e.userId = :userId AND e.startDate >= :startDate AND e.endDate <= :endDate ORDER BY e.startDate ASC",
//                Event.class
//            );
//            query.setParameter("userId", userId);
//            query.setParameter("startDate", startDate);
//            query.setParameter("endDate", endDate);
//            return query.getResultList();
//        } finally {
//            em.close();
//        }
//    }
//
//    public List<Event> find7NearestEvents(UUID userId) {
//        EntityManager em = emf.createEntityManager();
//        try {
//            TypedQuery<Event> query = em.createQuery(
//                "SELECT e FROM Event e WHERE e.userId = :userId ORDER BY ABS(DATE_PART('day', e.startDate - CURRENT_DATE)) ASC",
//                Event.class
//            );
//            query.setParameter("userId", userId);
//            query.setMaxResults(7);
//            return query.getResultList();
//        } finally {
//            em.close();
//        }
//    }
//
//    public void create(Event event) {
//        EntityManager em = emf.createEntityManager();
//        try {
//            em.getTransaction().begin();
//            em.persist(event);
//            em.getTransaction().commit();
//        } finally {
//            em.close();
//        }
//    }
//
//    public void update(Event event) {
//        EntityManager em = emf.createEntityManager();
//        try {
//            em.getTransaction().begin();
//            em.merge(event);
//            em.getTransaction().commit();
//        } finally {
//            em.close();
//        }
//    }
//
//    public void delete(UUID id) {
//        EntityManager em = emf.createEntityManager();
//        try {
//            em.getTransaction().begin();
//            Event event = em.find(Event.class, id);
//            if (event != null) {
//                em.remove(event);
//            }
//            em.getTransaction().commit();
//        } finally {
//            em.close();
//        }
//    }
//}
