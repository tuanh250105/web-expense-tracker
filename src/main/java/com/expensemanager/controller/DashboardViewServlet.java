package com.expensemanager.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "dashboardViewServlet", value = "/overview")
public class DashboardViewServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Gán biến "view" để layout.jsp biết file nội dung cần include
        request.setAttribute("view", "/views/overview.jsp");

        // Chuyển tiếp tới layout chính
        request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
    }
}
