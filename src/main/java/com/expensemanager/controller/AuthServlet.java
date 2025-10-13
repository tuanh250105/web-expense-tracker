package com.expensemanager.controller;

import java.io.IOException;

import com.expensemanager.model.User;
import com.expensemanager.service.AuthService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns={"/auth/login","/auth/register"})
public class AuthServlet extends HttpServlet {
  private final AuthService service = new AuthService();

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    switch (req.getServletPath()) {
      case "/auth/login" -> login(req, resp);
      case "/auth/register" -> register(req, resp);
      default -> resp.sendError(404);
    }
  }

  private void login(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    User u = service.login(req.getParameter("email"), req.getParameter("password"));
    if (u == null) {
      req.setAttribute("error", "Sai email hoặc mật khẩu");
      req.getRequestDispatcher("/views/auth/login.jsp").forward(req, resp);
    } else {
      req.getSession(true).setAttribute("user", u);
      if ("ADMIN".equalsIgnoreCase(u.getRole())) {
        resp.sendRedirect(req.getContextPath() + "/admin");
      } else {
        resp.sendRedirect(req.getContextPath() + "/layout/layout.jsp");
      }
    }
  }

  private void register(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    try {
      service.register(
        req.getParameter("fullName"),
        req.getParameter("email"),
        req.getParameter("password"));
      req.setAttribute("success", "Tạo tài khoản thành công! Vui lòng đăng nhập.");
      req.getRequestDispatcher("/views/auth/login.jsp").forward(req, resp);
    } catch (IllegalArgumentException e) {
      req.setAttribute("error", e.getMessage());
      req.getRequestDispatcher("/views/auth/register.jsp").forward(req, resp);
    }
  }
}