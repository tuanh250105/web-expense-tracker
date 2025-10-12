package com.expensemanager.controller;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * AccountsManagementServlet - Servlet để hiển thị trang Accounts Management
 * Chỉ forward đến JSP view
 */
@WebServlet("/accounts-management")
public class AccountsManagementServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        System.out.println("✅ AccountsManagementServlet - Loading accounts management page");
        
        // Forward to accounts management view
        request.getRequestDispatcher("/views/accounts-management.jsp").forward(request, response);
    }
}
