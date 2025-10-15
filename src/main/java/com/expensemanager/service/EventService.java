//package com.expensemanager.service;
//
//import java.time.Instant;
//import java.util.List;
//import java.util.UUID;
//
//import com.expensemanager.dao.EventDAO;
//import com.expensemanager.model.Event;
//
//public class EventService {
//    private final EventDAO eventDAO = new EventDAO();
//
//    public Event getEventById(UUID id) {
//        return eventDAO.findById(id);
//    }
//
//    public List<Event> getEventsByUserId(UUID userId) {
//        return eventDAO.findByUserId(userId);
//    }
//
//    public List<Event> getEventsByUserIdAndDateRange(UUID userId, java.time.LocalDate startDate, java.time.LocalDate endDate) {
//        return eventDAO.findByUserIdAndDateRange(userId, startDate, endDate);
//    }
//
//    public List<Event> get7NearestEvents(UUID userId) {
//        return eventDAO.find7NearestEvents(userId);
//    }
//
//    public boolean createEvent(Event event) {
//        event.setCreatedAt(Instant.now());
//        event.setUpdatedAt(Instant.now());
//        eventDAO.create(event);
//        return true;
//    }
//
//    public boolean updateEvent(Event event, UUID userId) {
//        Event existing = eventDAO.findById(event.getId());
//        if (existing == null || !existing.getUserId().equals(userId)) {
//            return false;
//        }
//        event.setCreatedAt(existing.getCreatedAt());
//        event.setUpdatedAt(Instant.now());
//        eventDAO.update(event);
//        return true;
//    }
//
//    public boolean deleteEvent(UUID eventId, UUID userId) {
//        Event existing = eventDAO.findById(eventId);
//        if (existing == null || !existing.getUserId().equals(userId)) {
//            return false;
//        }
//        eventDAO.delete(eventId);
//        return true;
//    }
//}
