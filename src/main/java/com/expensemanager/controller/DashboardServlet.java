package com.expensemanager.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@WebServlet(name = "dashboardServlet", value = "/dashboard-data")
public class DashboardServlet extends HttpServlet {

    private Gson gson;
    private EntityManagerFactory emf;
    private DashboardService dashboardService;

    @Override
    public void init() throws ServletException {
        gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            // Giữ lại phần khởi tạo này để tương thích, dù service đang dùng dữ liệu giả
            emf = Persistence.createEntityManagerFactory("supabase-pu");
            EntityManager em = emf.createEntityManager();

            dashboardService = new DashboardService();

            // Inject EntityManager vào service
            var f2 = DashboardService.class.getDeclaredField("em");
            f2.setAccessible(true);
            f2.set(dashboardService, em);

        } catch (Exception e) {
            throw new ServletException("Lỗi khi inject EntityManager", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String period = request.getParameter("period");
        Map<String, Object> data;

        try {
            if (period == null || period.isEmpty()) {
                period = "month"; // Mặc định là tháng
            }

            // Logic phân tích tham số period chi tiết
            if (period.startsWith("month-")) {
                String[] parts = period.split("-");
                int year = Integer.parseInt(parts[1]);
                int month = Integer.parseInt(parts[2]);
                data = dashboardService.getOverviewData(year, month);
            } else if (period.startsWith("week-")) {
                String[] parts = period.split("-");
                int year = Integer.parseInt(parts[1]);
                int month = Integer.parseInt(parts[2]);
                int week = Integer.parseInt(parts[3]);
                data = dashboardService.getOverviewData(year, month, week);
            } else {
                // Xử lý các trường hợp cũ: "week", "month", "year"
                data = dashboardService.getOverviewData(period);
            }
        } catch (Exception e) {
            e.printStackTrace();
            data = Collections.singletonMap("error", "Đã có lỗi xảy ra ở server: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        // PHẦN SỬA LỖI QUAN TRỌNG:
        // Dữ liệu trả về từ service đã bao gồm tất cả các mục, kể cả 'groupExpenses'.
        // Ta chỉ cần trả về toàn bộ map này mà không cần gọi thêm hàm nào khác.
        response.getWriter().write(gson.toJson(data));
    }


    @Override
    public void destroy() {
        if (emf != null) emf.close();
    }
}

