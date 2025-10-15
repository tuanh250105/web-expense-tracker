package com.expensemanager.controller;

import com.expensemanager.model.DaySummary;
import com.expensemanager.service.TransactionService;
import com.expensemanager.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import java.util.UUID;

@WebServlet(name = "CalendarViewServlet", value = "/calendar")
public class CalendarViewServlet extends HttpServlet {
    private final TransactionService transactionService = new TransactionService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (User) (session != null ? session.getAttribute("user") : null);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/views/auth/login.jsp");
            return;
        }
        UUID userId = user.getId();

        YearMonth currentMonth = YearMonth.now();
        Map<LocalDate, DaySummary> daySummaryMap = transactionService.getDaySummaries(userId, currentMonth);

        req.setAttribute("daySummaryMap", daySummaryMap);
        req.setAttribute("currentMonth", currentMonth);
        req.setAttribute("maxDay", currentMonth.lengthOfMonth());
        req.getRequestDispatcher("/views/calendar.jsp").forward(req, resp);
    }
}
