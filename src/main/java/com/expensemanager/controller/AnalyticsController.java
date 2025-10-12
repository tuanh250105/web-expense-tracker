package com.expensemanager.controller;

import com.expensemanager.model.Transaction;
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
import java.util.stream.Collectors;

@WebServlet(name = "AnalyticsController", urlPatterns = {"/analytics", "/api/analytics"})
public class AnalyticsController extends HttpServlet {
    private static final Gson GSON = GsonFactory.create();
    private final AnalyticsService service = new AnalyticsService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        if ("/analytics".equals(req.getServletPath())) {
            req.setAttribute("view", "/views/analytics.jsp");
            req.getRequestDispatcher("/layout/layout.jsp").forward(req, resp);
            return;
        }

        resp.setContentType("application/json;charset=UTF-8");

        String type = param(req, "type", "all");
        LocalDateTime from = parse(req.getParameter("from"));
        LocalDateTime to = parse(req.getParameter("to"));
        String group = param(req, "group", "day");

        // 🔹 Lấy userId (ưu tiên session, fallback parameter hoặc ID test)
        UUID userId = null;
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            Object val = session.getAttribute("userId");
            if (val instanceof UUID uid) userId = uid;
            else if (val instanceof String s && !s.isBlank()) {
                try { userId = UUID.fromString(s); } catch (Exception ignored) {}
            }
        }
        if (userId == null) {
            try {
                userId = UUID.fromString(req.getParameter("userId"));
            } catch (Exception e) {
                userId = UUID.fromString("67b78d51-4eec-491c-bbf0-30e982def9e0");
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();

        try {
            List<Transaction> list = service.find(userId, type, from, to);
            System.out.println("AnalyticsController → Query size = " + list.size());

            if (list.isEmpty()) {
                result.put("message", "No data found");
            } else {
                // ===== Tổng thu - chi =====
                double income = list.stream()
                        .filter(t -> "income".equalsIgnoreCase(t.getType()))
                        .mapToDouble(Transaction::getAmount)
                        .sum();
                double expense = list.stream()
                        .filter(t -> "expense".equalsIgnoreCase(t.getType()))
                        .mapToDouble(Transaction::getAmount)
                        .sum();
                double balance = income - expense;

                // ===== Nhóm theo ngày/tháng/năm =====
                Map<String, Double> groupedByTime = new TreeMap<>();
                for (Transaction t : list) {
                    if (t.getTransactionDate() == null) continue;
                    String key;
                    switch (group.toLowerCase()) {
                        case "month":
                            key = String.format("%d-%02d",
                                    t.getTransactionDate().getYear(),
                                    t.getTransactionDate().getMonthValue());
                            break;
                        case "year":
                            key = String.valueOf(t.getTransactionDate().getYear());
                            break;
                        default: // day
                            key = t.getTransactionDate().toLocalDate().toString();
                            break;
                    }
                    double amt = "income".equalsIgnoreCase(t.getType())
                            ? t.getAmount()
                            : -t.getAmount();
                    groupedByTime.merge(key, amt, Double::sum);
                }

                List<Map<String, Object>> groupedList = groupedByTime.entrySet().stream()
                        .map(e -> {
                            Map<String, Object> m = new LinkedHashMap<>();
                            m.put("label", e.getKey());
                            m.put("value", e.getValue());
                            return m;
                        })
                        .collect(Collectors.toList());

                // ===== Top danh mục =====
                Map<String, Double> groupedCategory = list.stream()
                        .collect(Collectors.groupingBy(
                                t -> (t.getCategory() != null && t.getCategory().getName() != null)
                                        ? t.getCategory().getName()
                                        : "Không xác định",
                                Collectors.summingDouble(Transaction::getAmount)
                        ));

                int topN = 10;
                try {
                    topN = Integer.parseInt(req.getParameter("top"));
                } catch (Exception ignored) {}

                List<Map<String, Object>> topCategory = groupedCategory.entrySet().stream()
                        .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                        .limit(topN)
                        .map(e -> {
                            Map<String, Object> m = new LinkedHashMap<>();
                            m.put("categoryName", e.getKey());
                            m.put("total", e.getValue());
                            return m;
                        })
                        .collect(Collectors.toList());

                // ===== Dữ liệu gốc (raw) =====
                List<Map<String, Object>> safeList = list.stream().map(t -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", t.getId());
                    item.put("type", t.getType());
                    item.put("amount", t.getAmount());
                    item.put("note", t.getNote());
                    item.put("date", t.getTransactionDate());
                    item.put("category", (t.getCategory() != null)
                            ? t.getCategory().getName() : null);
                    return item;
                }).collect(Collectors.toList());

                // ===== Gắn vào JSON =====
                result.put("summary", Map.of(
                        "income", income,
                        "expense", expense,
                        "balance", balance
                ));
                result.put("grouped", groupedList);
                result.put("topCategory", topCategory);
                result.put("raw", safeList);
            }

            String json = GSON.toJson(result);
            try (PrintWriter out = resp.getWriter()) {
                out.write(json);
                out.flush();
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            String errJson = "{\"error\":\"" + e.getMessage().replace("\"", "'") + "\"}";
            resp.getWriter().write(errJson);
            resp.getWriter().flush();
        }
    }

    // ===== Helper =====
    private String param(HttpServletRequest req, String k, String def) {
        String v = req.getParameter(k);
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
}
