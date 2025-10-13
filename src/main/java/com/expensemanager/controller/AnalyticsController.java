package com.expensemanager.controller;

import com.expensemanager.service.AnalyticsService;
import com.expensemanager.util.GsonFactory;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@WebServlet(name = "AnalyticsController", urlPatterns = {"/analytics", "/api/analytics"})
public class AnalyticsController extends HttpServlet {
    private static final Gson GSON = GsonFactory.create();
    private final AnalyticsService service = new AnalyticsService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        // Nếu là request UI
        if ("/analytics".equals(req.getServletPath())) {
            req.setAttribute("view", "/views/analytics.jsp");
            req.getRequestDispatcher("/layout/layout.jsp").forward(req, resp);
            return;
        }

        resp.setContentType("application/json;charset=UTF-8");

        try {
            UUID userId = resolveUserId(req);
            String type = param(req, "type", "all");
            LocalDateTime from = parse(req.getParameter("from"));
            LocalDateTime to = parse(req.getParameter("to"));
            String group = param(req, "group", "day");
            int topN = 10;
            try { topN = Integer.parseInt(req.getParameter("top")); } catch (Exception ignored) {}

            Map<String, Object> result = service.buildAnalytics(userId, type, from, to, group, topN);

            try (PrintWriter out = resp.getWriter()) {
                out.write(GSON.toJson(result));
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + e.getMessage().replace("\"", "'") + "\"}");
        }
    }

    // ===== Helper =====
    private String param(HttpServletRequest req, String key, String def) {
        String v = req.getParameter(key);
        return (v == null || v.isEmpty()) ? def : v;
    }

    private LocalDateTime parse(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return LocalDateTime.parse(s + "T00:00:00");
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private UUID resolveUserId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            Object u = session.getAttribute("user");
            if (u instanceof com.expensemanager.model.User user && user.getId() != null)
                return user.getId();

            Object id = session.getAttribute("userId");
            if (id instanceof UUID uid) return uid;
            if (id instanceof String s)
                try { return UUID.fromString(s); } catch (Exception ignored) {}
        }

        try {
            return UUID.fromString(req.getParameter("userId"));
        } catch (Exception e) {
            return UUID.fromString("67b78d51-4eec-491c-bbf0-30e982def9e0"); // fallback user test
        }
    }
}
