package com.expensemanager.controller;

import com.expensemanager.dao.TransactionDAO;
import com.expensemanager.model.Transaction;
import com.expensemanager.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * CalendarServlet - hiển thị lịch tổng hợp thu/chi theo ngày và chi tiết theo ngày.
 * Phù hợp Jakarta EE 10, sử dụng DAO với JpaUtil nội bộ.
 */
@WebServlet("/calendar")
public class CalendarServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(true);
        User user = (User) session.getAttribute("user");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/views/auth/login.jsp");
            return;
        }
        UUID userId = user.getId();

        // Lấy tham số month, year, date nếu có
        String monthParam = req.getParameter("month");
        String yearParam = req.getParameter("year");
        String dateParam = req.getParameter("date");

        YearMonth ym;
        if (monthParam != null && yearParam != null) {
            try {
                ym = YearMonth.of(Integer.parseInt(yearParam), Integer.parseInt(monthParam));
            } catch (NumberFormatException e) {
                ym = YearMonth.now();
            }
        } else {
            ym = YearMonth.now();
        }

        LocalDate selectedDate = null;
        if (dateParam != null && !dateParam.isBlank()) {
            try {
                selectedDate = LocalDate.parse(dateParam);
            } catch (Exception ignored) {
            }
        }

        TransactionDAO transactionDAO = new TransactionDAO();
        Map<LocalDate, BigDecimal[]> summary = transactionDAO.getDailySummary(userId, ym.getMonthValue(), ym.getYear());

        List<Transaction> transactions = null;
        if (selectedDate != null) {
            transactions = transactionDAO.getTransactionsByDate(userId, selectedDate);
        }

        req.setAttribute("summary", summary);
        req.setAttribute("month", ym.getMonthValue());
        req.setAttribute("year", ym.getYear());
        req.setAttribute("selectedDate", selectedDate);
        req.setAttribute("transactions", transactions);

        // Set view để render qua layout chung
        req.setAttribute("view", "/views/calendar.jsp");
        req.getRequestDispatcher("/layout/layout.jsp").forward(req, resp);
    }
}
