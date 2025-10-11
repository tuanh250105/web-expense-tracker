package com.expensemanager.controller;

import com.expensemanager.model.Transaction;
import com.expensemanager.service.AnalyticsService;
import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.OffsetDateTime;
import java.util.List;

@WebServlet(name = "AnalyticsApiServlet", urlPatterns = {"/api/analytics"})
public class AnalyticsController extends HttpServlet {
    private static final Gson GSON = new Gson();
    private final AnalyticsService service = new AnalyticsService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("⚡ AnalyticsController: doGet() START");
        resp.setContentType("application/json;charset=UTF-8");
        String type = param(req, "type", "all");
        OffsetDateTime from = parse(req.getParameter("from"));
        OffsetDateTime to   = parse(req.getParameter("to"));
        List<Transaction> data = service.find(type, from, to);

        try (PrintWriter out = resp.getWriter()) {
            out.print(GSON.toJson(data));
        }
    }

    private static String param(HttpServletRequest req, String key, String def) {
        String val = req.getParameter(key);
        return (val == null || val.isBlank()) ? def : val;
    }
    private static OffsetDateTime parse(String s) {
        try { return (s == null || s.isBlank()) ? null : OffsetDateTime.parse(s); }
        catch (Exception e) { return null; }
    }
}
