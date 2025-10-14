package com.expensemanager.controller;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "CalendarViewServlet", value = "/calendar")
public class CalendarViewServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("view", "/views/calendar.jsp");

    // 2. Forward đến file layout chính
    req.getRequestDispatcher("/layout/layout.jsp").forward(req, resp);
    }
    
}
