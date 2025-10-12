package com.expensemanager.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * REST API cho Top Categories
 * Endpoint: /api/top-categories
 */
@WebServlet(name = "TopCategoriesAPIServlet", value = "/api/top-categories")
public class TopCategoriesAPIServlet extends HttpServlet {
    
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
        
        response.setContentType("application/json;charset=UTF-8");
        
        try {
            // Kiểm tra session
            HttpSession session = request.getSession(false);
            UUID userId = null;
            
            if (session != null && session.getAttribute("user_id") != null) {
                userId = (UUID) session.getAttribute("user_id");
            }
            
            // Development: Dùng UUID test
            if (userId == null) {
                userId = UUID.fromString("67b78d51-4eec-491c-bbf0-30e982def9e0");
            }
            
            // Lấy tháng/năm từ query params
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
            
            System.out.println("📅 Fetching top categories for userId: " + userId);
            System.out.println("📅 Date range: " + startOfMonth + " to " + endOfMonth);
            
            // Lấy top categories
            List<CategoryStats> topCategories = transactionService.getTopCategoriesByMonth(
                userId, startOfMonth, endOfMonth
            );
            
            System.out.println("📊 Found " + topCategories.size() + " top categories");
            if (topCategories.isEmpty()) {
                System.out.println("⚠️ No transactions found in this date range for user " + userId);
            }
            
            // Tạo response
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("data", topCategories);
            responseData.put("month", yearMonth.getMonthValue());
            responseData.put("year", yearMonth.getYear());
            responseData.put("count", topCategories.size());
            
            response.getWriter().write(gson.toJson(responseData));
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(errorResponse));
        }
    }
}
