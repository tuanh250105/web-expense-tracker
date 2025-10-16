package com.expensemanager.controller;

import com.expensemanager.dao.*;
import com.expensemanager.model.User;
import com.expensemanager.service.DashboardService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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

    @Override
    public void init() {
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            HttpSession session = request.getSession(true);
            User user = (User) session.getAttribute("user");

            if (user == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(gson.toJson(Collections.singletonMap("error", "Người dùng chưa đăng nhập")));
                return;
            }

            UUID userId = user.getId();

            // ✅ Không cần EntityManager ở servlet nữa
            // DAO tự xử lý EntityManager bằng JpaUtil bên trong
            TransactionDAO transactionDAO = new TransactionDAO();
            GroupDAO groupDAO = new GroupDAO();
            UserDAO userDAO = new UserDAO();
            AccountDAO accountDAO = new AccountDAO();

            // Dịch vụ tổng hợp dashboard
            DashboardService dashboardService = new DashboardService(userId, transactionDAO, groupDAO, userDAO, accountDAO);

            String period = request.getParameter("period");
            Map<String, Object> data;

            // ✅ Xử lý logic cho tham số period
            if (period != null && (period.startsWith("month-") || period.startsWith("week-") || period.startsWith("year-"))) {
                try {
                    String[] parts = period.split("-");
                    String type = parts[0];
                    int year = Integer.parseInt(parts[1]);

                    if ("year".equals(type)) {
                        data = dashboardService.getOverviewData(year);
                    } else if ("month".equals(type)) {
                        int month = Integer.parseInt(parts[2]);
                        data = dashboardService.getOverviewData(year, month);
                    } else if ("week".equals(type) && parts.length > 3) {
                        int month = Integer.parseInt(parts[2]);
                        int week = Integer.parseInt(parts[3]);
                        data = dashboardService.getOverviewData(year, month, week);
                    } else {
                        data = dashboardService.getOverviewData("month");
                    }
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write(gson.toJson(Collections.singletonMap(
                            "error", "Tham số period không hợp lệ: " + period
                    )));
                    return;
                }
            } else {
                // Trường hợp period đơn giản hoặc mặc định
                if (period == null || period.isEmpty()) {
                    period = "month";
                }
                data = dashboardService.getOverviewData(period);
            }

            response.getWriter().write(gson.toJson(data));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Đã xảy ra lỗi khi lấy dữ liệu dashboard", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(Collections.singletonMap(
                    "error", "Đã có lỗi xảy ra ở server: " + e.getMessage()
            )));
        }
    }
}
