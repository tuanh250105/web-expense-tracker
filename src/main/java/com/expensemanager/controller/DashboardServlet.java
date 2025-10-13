package com.expensemanager.controller;

import com.expensemanager.dao.*;
import com.expensemanager.service.DashboardService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "dashboardServlet", value = "/dashboard-data")
public class DashboardServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(DashboardServlet.class.getName());
    private Gson gson;
    private EntityManagerFactory emf;

    @Override
    public void init() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        emf = Persistence.createEntityManagerFactory("default");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        EntityManager em = null;
        try {
            em = emf.createEntityManager();

            // Khởi tạo DAO
            TransactionDAO transactionDAO = new TransactionDAO();
            GroupDAO groupDAO = new GroupDAO(em);
            UserDAO userDAO = new UserDAO(em);
            AccountDAO accountDAO = new AccountDAO( );

            DashboardService dashboardService = new DashboardService(transactionDAO, groupDAO, userDAO, accountDAO);

            String period = request.getParameter("period");
            Map<String, Object> data;

            // SỬA LỖI: Logic mới để phân tích tham số `period` từ JavaScript
            if (period != null && (period.startsWith("month-") || period.startsWith("week-"))) {
                try {
                    String[] parts = period.split("-");
                    String type = parts[0];
                    int year = Integer.parseInt(parts[1]);
                    int month = Integer.parseInt(parts[2]);

                    if ("month".equals(type)) {
                        data = dashboardService.getOverviewData(year, month);
                    } else if ("week".equals(type) && parts.length > 3) {
                        int week = Integer.parseInt(parts[3]);
                        data = dashboardService.getOverviewData(year, month, week);
                    } else {
                        // Fallback an toàn nếu period không hợp lệ
                        data = dashboardService.getOverviewData("month");
                    }
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write(gson.toJson(Collections.singletonMap("error", "Tham số period không hợp lệ: " + period)));
                    return;
                }
            } else {
                // Xử lý các trường hợp period đơn giản (month, week, year) và trường hợp mặc định
                if (period == null || period.isEmpty()) {
                    period = "month";
                }
                data = dashboardService.getOverviewData(period);
            }

            response.getWriter().write(gson.toJson(data));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Đã xảy ra lỗi khi lấy dữ liệu dashboard", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(Collections.singletonMap("error", "Đã có lỗi xảy ra ở server: " + e.getMessage())));
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public void destroy() {
        if (emf != null) {
            emf.close();
        }
    }
}

