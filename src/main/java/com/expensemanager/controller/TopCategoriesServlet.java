package com.expensemanager.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import com.expensemanager.service.TransactionService;
import com.expensemanager.service.TransactionService.CategoryStats;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * TopCategoriesServlet - Hiển thị thống kê Top Categories
 * Dựa vào Transaction data để tính toán
 */
@WebServlet(name = "TopCategoriesServlet", value = "/top-categories")
public class TopCategoriesServlet extends HttpServlet {
    
    private TransactionService transactionService;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        this.transactionService = new TransactionService();
        this.gson = new Gson();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        
        // Kiểm tra session (optional - nếu cần authentication)
        HttpSession session = request.getSession(false);
        UUID userId = null;
        
        if (session != null && session.getAttribute("user_id") != null) {
            userId = (UUID) session.getAttribute("user_id");
        }
        
        // Nếu không có session, dùng userId test (development mode)
        if (userId == null) {
            // TODO: Redirect to login page khi có authentication
            // response.sendRedirect(request.getContextPath() + "/login");
            // return;
            
            // Development: Dùng UUID test
            userId = UUID.fromString("67b78d51-4eec-491c-bbf0-30e982def9e0");
        }
        
        // Lấy tháng/năm từ query params (mặc định: tháng hiện tại)
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
        
        // Lấy top categories
        List<CategoryStats> topCategories = transactionService.getTopCategoriesByMonth(
            userId, startOfMonth, endOfMonth
        );
        
        // Set attributes để JSP sử dụng
        request.setAttribute("topCategories", topCategories);
        request.setAttribute("month", yearMonth.getMonthValue());
        request.setAttribute("year", yearMonth.getYear());
        request.setAttribute("pageTitle", "Thống kê Top Categories");
        
        // Forward đến JSP
        request.getRequestDispatcher("/views/top-categories.jsp").forward(request, response);
    }
}
