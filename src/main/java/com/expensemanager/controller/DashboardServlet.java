package com.expensemanager.controller;

import com.expensemanager.dao.*;
import com.expensemanager.service.DashboardService;
import com.expensemanager.util.JpaUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
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
        this.emf = JpaUtil.getEntityManagerFactory();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write("{\"status\":\"ok\"}");

        /* Temporarily commented out for testing
        EntityManager em = null;
        try {
            em = emf.createEntityManager();

            TransactionDAO transactionDAO = new TransactionDAO();
            AccountDAO accountDAO = new AccountDAO();
            GroupDAO groupDAO = new GroupDAO(em);
            UserDAO userDAO = new UserDAO(em);

            DashboardService dashboardService = new DashboardService(transactionDAO, groupDAO, userDAO, accountDAO);

            String period = request.getParameter("period");
            Map<String, Object> data;

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
                        data = dashboardService.getOverviewData("month");
                    }
                } catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write(gson.toJson(Collections.singletonMap("error", "Invalid period parameter: " + period)));
                    return;
                }
            } else {
                if (period == null || period.isEmpty()) {
                    period = "month";
                }
                data = dashboardService.getOverviewData(period);
            }

            response.getWriter().write(gson.toJson(data));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching dashboard data", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(Collections.singletonMap("error", "Server error: " + e.getMessage())));
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        */
    }

    @Override
    public void destroy() {
        // JpaUtil manages the lifecycle of the EntityManagerFactory
    }
}
