package com.expensemanager.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Servlet này xử lý yêu cầu hiển thị trang "Chi tiêu nhóm".
 * Nó hoạt động theo kiến trúc layout chung.
 */
@WebServlet(name = "groupExpenseViewServlet", value = "/group_expense")
public class GroupExpenseViewServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Gán biến "view" để layout.jsp biết file nội dung cần include
        request.setAttribute("view", "/views/group_expense.jsp");

        // 2. Chuyển tiếp (forward) tới file layout chính để render toàn bộ trang
        request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
    }
}