package com.expensemanager.controller;

import com.expensemanager.model.Event;
import com.expensemanager.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@WebServlet(urlPatterns = {"/api/events", "/api/events/*", "/api/events/notifications"})
public class EventController extends HttpServlet {
    private final EventService eventService = new EventService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        String path = req.getPathInfo();
        String userIdStr = req.getParameter("user_id");
        if (userIdStr == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Missing user_id\"}");
            return;
        }
        UUID userId = UUID.fromString(userIdStr);
        if (req.getRequestURI().endsWith("/notifications")) {
            List<Event> events = eventService.get7NearestEvents(userId);
            out.write(objectMapper.writeValueAsString(events));
            return;
        }
        String startDateStr = req.getParameter("startDate");
        String endDateStr = req.getParameter("endDate");
        if (startDateStr != null && endDateStr != null) {
            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate endDate = LocalDate.parse(endDateStr);
            List<Event> events = eventService.getEventsByUserIdAndDateRange(userId, startDate, endDate);
            out.write(objectMapper.writeValueAsString(events));
        } else {
            List<Event> events = eventService.getEventsByUserId(userId);
            out.write(objectMapper.writeValueAsString(events));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        Event event = objectMapper.readValue(req.getReader(), Event.class);
        if (event.getUserId() == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Missing user_id\"}");
            return;
        }
        boolean success = eventService.createEvent(event);
        if (success) {
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write(objectMapper.writeValueAsString(event));
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Create failed\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Missing event id\"}");
            return;
        }
        UUID eventId = UUID.fromString(pathInfo.substring(1));
        Event event = objectMapper.readValue(req.getReader(), Event.class);
        if (event.getUserId() == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Missing user_id\"}");
            return;
        }
        event.setId(eventId);
        boolean success = eventService.updateEvent(event, event.getUserId());
        if (success) {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(objectMapper.writeValueAsString(event));
        } else {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"error\":\"Update failed or not allowed\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        String pathInfo = req.getPathInfo();
        String userIdStr = req.getParameter("user_id");
        if (pathInfo == null || pathInfo.length() <= 1 || userIdStr == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Missing event id or user_id\"}");
            return;
        }
        UUID eventId = UUID.fromString(pathInfo.substring(1));
        UUID userId = UUID.fromString(userIdStr);
        boolean success = eventService.deleteEvent(eventId, userId);
        if (success) {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"success\":true}");
        } else {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"error\":\"Delete failed or not allowed\"}");
        }
    }
}
