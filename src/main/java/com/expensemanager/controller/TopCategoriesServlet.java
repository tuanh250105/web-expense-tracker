package com.expensemanager.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.expensemanager.service.TransactionServicestart;
import com.expensemanager.service.TransactionServicestart.CategoryStats;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * TopCategoriesServlet - Xử lý cả HTML view và JSON API
 */
@WebServlet(name = "TopCategoriesServlet", urlPatterns = {"/top-categories", "/api/top-categories"})
public class TopCategoriesServlet extends HttpServlet {

    private TransactionServicestart transactionService;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        this.transactionService = new TransactionServicestart();
        this.gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();
        System.out.println("✅ TopCategoriesServlet initialized!");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Kiểm tra xem có phải request API không
        String path = request.getServletPath();
        boolean isApiRequest = path.startsWith("/api/");

        // Lấy userId từ session
        HttpSession session = request.getSession(false);
        UUID userId = null;

        if (session != null && session.getAttribute("user_id") != null) {
            userId = (UUID) session.getAttribute("user_id");
        }

        // Development: Dùng UUID test nếu chưa login
        if (userId == null) {
            userId = UUID.fromString("67b78d51-4eec-491c-bbf0-30e982def9e0");
            System.out.println("⚠️ Using test userId: " + userId);
        }

        // Lấy tham số month, year
        String monthParam = request.getParameter("month");
        String yearParam = request.getParameter("year");
        String periodParam = request.getParameter("period");

        YearMonth yearMonth = null;
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        // Xử lý period=all (lấy tất cả dữ liệu)
        if ("all".equals(periodParam)) {
            // Lấy từ 2020 đến hiện tại (hoặc tùy chỉnh)
            startDate = LocalDateTime.of(2020, 1, 1, 0, 0, 0);
            endDate = LocalDateTime.now();
            System.out.println("📅 Fetching all-time data");
        } else if (monthParam != null && yearParam != null) {
            // Lấy theo tháng cụ thể
            try {
                yearMonth = YearMonth.of(Integer.parseInt(yearParam), Integer.parseInt(monthParam));
                startDate = yearMonth.atDay(1).atStartOfDay();
                endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);
                System.out.println("📅 Fetching data for: " + yearMonth);
            } catch (NumberFormatException e) {
                yearMonth = YearMonth.now();
                startDate = yearMonth.atDay(1).atStartOfDay();
                endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);
            }
        } else {
            // Mặc định: tháng hiện tại
            yearMonth = YearMonth.now();
            startDate = yearMonth.atDay(1).atStartOfDay();
            endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);
            System.out.println("📅 Fetching data for current month: " + yearMonth);
        }

        try {
            // Lấy dữ liệu từ service
            List<CategoryStats> topCategories = transactionService.getTopCategoriesByMonth(
                    userId, startDate, endDate
            );

            System.out.println("📊 Found " + topCategories.size() + " categories");

            // Nếu là API request -> trả về JSON
            if (isApiRequest) {
                response.setContentType("application/json;charset=UTF-8");
                response.setCharacterEncoding("UTF-8");

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("data", topCategories);
                result.put("count", topCategories.size());
                result.put("month", yearMonth != null ? yearMonth.getMonthValue() : null);
                result.put("year", yearMonth != null ? yearMonth.getYear() : null);

                response.getWriter().write(gson.toJson(result));
                System.out.println("✅ API response sent with " + topCategories.size() + " items");
            } 
            // Nếu là HTML request -> render view
            else {
                response.setContentType("text/html;charset=UTF-8");
                
                request.setAttribute("topCategories", topCategories);
                request.setAttribute("month", yearMonth != null ? yearMonth.getMonthValue() : null);
                request.setAttribute("year", yearMonth != null ? yearMonth.getYear() : null);
                request.setAttribute("pageTitle", "Thống kê Top Categories");
                request.setAttribute("view", "/views/top-categories.jsp");

                request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
                System.out.println("✅ HTML view rendered");
            }

        } catch (Exception e) {
            System.err.println("❌ Error in TopCategoriesServlet: " + e.getMessage());
            e.printStackTrace();

            if (isApiRequest) {
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", e.getMessage());
                
                response.getWriter().write(gson.toJson(error));
            } else {
                throw new ServletException("Error loading top categories", e);
            }
        }
    }

    @Override
    public void destroy() {
        System.out.println("✅ TopCategoriesServlet destroyed!");
    }
}