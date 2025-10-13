package com.expensemanager.controller;

import com.expensemanager.model.Event;
import com.expensemanager.model.Point;
import com.expensemanager.service.EventService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/events", "/events/*", "/points/redeem"})
public class EventController extends HttpServlet {
    private final EventService eventService = new EventService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        Long userId = getUserIdAsLong(session.getAttribute("user_id"));

        String path = request.getPathInfo();
        if (path != null && path.matches("/^\\/\\d+\\/series$/")) {
            Long eventId = Long.valueOf(path.split("/")[1]);
            Map<String, Object> series = eventService.getEventSpendingSeries(eventId, userId);
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.write(toJson(series));
            out.flush();
            return;
        }

        List<Event> events = eventService.listEvents(userId);
        Point p = eventService.getPoints(userId);
        request.setAttribute("events", events);
        request.setAttribute("points", p);
        request.setAttribute("view", "/views/events.jsp");
        request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        Long userId = getUserIdAsLong(session.getAttribute("user_id"));

        String servletPath = request.getServletPath();
        if ("/points/redeem".equals(servletPath)) {
            int amount = Integer.parseInt(request.getParameter("amount"));
            boolean ok = eventService.redeemPoints(userId, amount);
            response.setContentType("application/json");
            response.getWriter().write("{\"ok\":" + ok + "}");
            return;
        }

        String path = request.getPathInfo();
        if (path != null && path.endsWith("/attach-tx")) {
            Long eventId = Long.valueOf(path.split("/")[1]);
            Long txId = Long.valueOf(request.getParameter("transactionId"));
            eventService.attachTransactionToEvent(eventId, txId);
            response.sendRedirect(request.getContextPath() + "/events");
            return;
        }

        String action = request.getParameter("action");
        if ("create".equalsIgnoreCase(action)) {
            String name = request.getParameter("name");
            String goal = request.getParameter("goalAmount");
            BigDecimal goalAmount = (goal == null || goal.isEmpty()) ? null : new BigDecimal(goal);
            LocalDate start = empty(request.getParameter("startDate")) ? null : LocalDate.parse(request.getParameter("startDate"));
            LocalDate end = empty(request.getParameter("endDate")) ? null : LocalDate.parse(request.getParameter("endDate"));
            eventService.createEvent(userId, name, goalAmount, start, end);
            response.sendRedirect(request.getContextPath() + "/events");
        } else if ("archive".equalsIgnoreCase(action)) {
            Long id = Long.valueOf(request.getParameter("id"));
            eventService.archiveEvent(id);
            response.sendRedirect(request.getContextPath() + "/events");
        } else {
            response.sendError(400, "Unsupported action");
        }
    }

    private boolean empty(String s) { return s == null || s.isEmpty(); }

    private Long getUserIdAsLong(Object sessionVal) {
        if (sessionVal instanceof Long) return (Long) sessionVal;
        if (sessionVal instanceof String) return Long.valueOf((String) sessionVal);
        return 0L;
    }

    private String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        int i = 0;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (i++ > 0) sb.append(",");
            sb.append("\"").append(e.getKey()).append("\":");
            Object v = e.getValue();
            if (v instanceof String[]) {
                String[] arr = (String[]) v;
                sb.append("[");
                for (int j = 0; j < arr.length; j++) {
                    if (j > 0) sb.append(",");
                    sb.append("\"").append(arr[j]).append("\"");
                }
                sb.append("]");
            } else if (v instanceof Object[]) {
                Object[] arr = (Object[]) v;
                sb.append("[");
                for (int j = 0; j < arr.length; j++) {
                    if (j > 0) sb.append(",");
                    sb.append(String.valueOf(arr[j]));
                }
                sb.append("]");
            } else {
                sb.append("\"").append(String.valueOf(v)).append("\"");
            }
        }
        sb.append("}");
        return sb.toString();
    }
}


