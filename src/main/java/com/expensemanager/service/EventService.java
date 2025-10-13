package com.expensemanager.service;

import com.expensemanager.dao.EventDAO;
import com.expensemanager.dao.PointDAO;
import com.expensemanager.model.Event;
import com.expensemanager.model.Point;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventService {
    private final EventDAO eventDAO = new EventDAO();
    private final PointDAO pointDAO = new PointDAO();

    public Long createEvent(Long userId, String name, BigDecimal goalAmount, LocalDate start, LocalDate end) {
        Event e = new Event();
        e.setUserId(userId);
        e.setName(name);
        e.setGoalAmount(goalAmount);
        e.setStartDate(start);
        e.setEndDate(end);
        e.setStatus("ACTIVE");
        return eventDAO.create(e);
    }

    public void archiveEvent(Long eventId) { eventDAO.archive(eventId); }

    public void attachTransactionToEvent(Long eventId, Long txId) { eventDAO.attachTransaction(eventId, txId); }

    public List<Event> listEvents(Long userId) { return eventDAO.listByUser(userId); }

    public Map<String, Object> getEventSpendingSeries(Long eventId, Long userId) {
        var list = eventDAO.getEventSeries(eventId, userId);
        Map<String, Object> res = new HashMap<>();
        res.put("labels", list.stream().map(d -> d.date.toString()).toArray(String[]::new));
        res.put("data", list.stream().map(d -> d.spent).toArray());
        return res;
    }

    public Point getPoints(Long userId) { return pointDAO.getOrCreate(userId); }

    public void addPointsForBudgetOnTime(Long userId, int score) { pointDAO.addPoints(userId, score); }

    public boolean redeemPoints(Long userId, int amount) { return pointDAO.redeem(userId, amount); }
}


