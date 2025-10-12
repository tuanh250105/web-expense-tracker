package com.expensemanager.controller;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * BankHistoryServlet - Servlet để hiển thị trang Bank History
 * Chỉ forward đến JSP view
 */
@WebServlet("/bank-history")
public class BankHistoryServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        System.out.println("✅ BankHistoryServlet - Loading bank history page");
        
        // Forward to bank history view
        request.getRequestDispatcher("/views/bank-history-content.jsp").forward(request, response);
    }
}
