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
        // 1. Đặt đường dẫn của trang con vào thuộc tính "view"
        request.setAttribute("view", "/views/bank-history-content.jsp");

        // 2. Forward đến file layout chính
        request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
    }
}
