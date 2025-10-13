package com.expensemanager.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "groupExpenseServlet", value = "/group_expense")
public class GroupExpenseViewServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Trả trực tiếp fragment JSP, KHÔNG đi qua layout
        request.getRequestDispatcher("/views/group_expense.jsp").forward(request, response);
    }
}

