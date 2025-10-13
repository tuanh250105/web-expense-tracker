package com.expensemanager.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import com.expensemanager.service.TransactionServicestart;
import com.expensemanager.service.TransactionServicestart.CategoryStats;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * TopCategoriesServlet - Hiển thị thống kê Top Categories
 * Sử dụng layout chung để hiển thị view.
 */
@WebServlet(name = "TopCategoriesServlet", value = "/top-categories")
public class TopCategoriesServlet extends HttpServlet {

    private TransactionServicestart transactionService;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        this.transactionService = new TransactionServicestart();
        this.gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        UUID userId = null;

        if (session != null && session.getAttribute("user_id") != null) {
            userId = (UUID) session.getAttribute("user_id");
        }

        if (userId == null) {
            // Development: Dùng UUID test
            userId = UUID.fromString("67b78d51-4eec-491c-bbf0-30e982def9e0");
        }

        String monthParam = request.getParameter("month");
        String yearParam = request.getParameter("year");

        YearMonth yearMonth;
        if (monthParam != null && yearParam != null) {
            yearMonth = YearMonth.of(Integer.parseInt(yearParam), Integer.parseInt(monthParam));
        } else {
            yearMonth = YearMonth.now();
        }

        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<CategoryStats> topCategories = transactionService.getTopCategoriesByMonth(
                userId, startOfMonth, endOfMonth
        );

        request.setAttribute("topCategories", topCategories);
        request.setAttribute("month", yearMonth.getMonthValue());
        request.setAttribute("year", yearMonth.getYear());
        request.setAttribute("pageTitle", "Thống kê Top Categories");

        // --- SỬA ĐỔI Ở ĐÂY ---
        // Đặt view cụ thể cho layout
        request.setAttribute("view", "/views/top-categories.jsp");

        // Forward tới layout chính để render trang hoàn chỉnh
        request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
    }
}