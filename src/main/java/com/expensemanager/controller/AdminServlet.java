package com.expensemanager.controller;

import java.io.IOException;

import com.expensemanager.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/admin")
public class AdminServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/views/auth/login.jsp?error=unauthorized");
            return;
        }

        // Lấy dữ liệu thống kê user
    com.expensemanager.repository.UserRepository repo = new com.expensemanager.repository.UserRepository();
    long totalUsers = repo.countAllUsers();
    long newUsersToday = repo.countNewUsersToday();
    long newUsersWeek = repo.countNewUsersThisWeek();
    java.util.List<com.expensemanager.repository.UserStat> userStatsByPeriod = repo.getUserStatsByPeriod();
    java.util.List<com.expensemanager.model.User> userList = repo.findAll();

    req.setAttribute("totalUsers", totalUsers);
    req.setAttribute("newUsersToday", newUsersToday);
    req.setAttribute("newUsersWeek", newUsersWeek);
    req.setAttribute("userStatsByPeriod", userStatsByPeriod);
    req.setAttribute("userList", userList);
    req.getRequestDispatcher("/views/admin.jsp").forward(req, resp);
    }
}
