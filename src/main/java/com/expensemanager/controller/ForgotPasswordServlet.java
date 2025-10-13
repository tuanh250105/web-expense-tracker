package com.expensemanager.controller;

import com.expensemanager.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet(urlPatterns = {"/auth/forgot", "/auth/reset"})
public class ForgotPasswordServlet extends HttpServlet {
    private final AuthService service = new AuthService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        if ("/auth/forgot".equals(path)) {
            String email = req.getParameter("email");
            boolean ok = service.sendOtpForReset(email);
            req.setAttribute(ok ? "success" : "error", ok ? "OTP has been sent to your email" : "Failed to send OTP");
            req.getRequestDispatcher("/views/auth/forgot.jsp").forward(req, resp);
        } else if ("/auth/reset".equals(path)) {
            String email = req.getParameter("email");
            String otp = req.getParameter("otp");
            String newPassword = req.getParameter("newPassword");
            boolean ok = service.resetPasswordWithOtp(email, otp, newPassword);
            if (ok) {
                req.setAttribute("success", "Password has been reset. Please login.");
                req.getRequestDispatcher("/views/auth/login.jsp").forward(req, resp);
            } else {
                req.setAttribute("error", "Invalid or expired OTP");
                req.getRequestDispatcher("/views/auth/reset.jsp").forward(req, resp);
            }
        } else {
            resp.sendError(404);
        }
    }
}
