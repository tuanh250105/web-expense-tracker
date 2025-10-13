package com.expensemanager.controller;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * AccountsManagementServlet - Servlet để hiển thị trang Accounts Management
 * Sử dụng layout chung để hiển thị view.
 */
@WebServlet("/accounts-management")
public class AccountsManagementServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("✅ AccountsManagementServlet - Loading accounts management page");

        // Đặt view cụ thể cho layout
        request.setAttribute("view", "/views/accounts-management.jsp");

        // Forward tới layout chính để render trang hoàn chỉnh
        request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
    }
}