package com.expensemanager.controller;

import com.expensemanager.service.StatementEmailService;
import com.expensemanager.service.EmailService;
import com.expensemanager.dao.TransactionDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@WebServlet("/send-statement")
public class StatementEmailController extends HttpServlet {

    private StatementEmailService statementEmailService;

    @Override
    public void init() throws ServletException {
        TransactionDAO transactionDAO = new TransactionDAO();
        EmailService emailService = new EmailService();
        statementEmailService = new StatementEmailService(transactionDAO, emailService);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Simulate current logged-in user (later replace with session-based user)
        String userEmail = "test@user.com";
        String username = "Test User";
        int userId = 123; // placeholder, until user IDs are UUID-based in session

        // ✅ Convert int userId → UUID for service compatibility
        UUID userUUID = UUID.nameUUIDFromBytes(String.valueOf(userId).getBytes());

        try {
            statementEmailService.sendMonthlyStatement(userEmail, username, userUUID);
            resp.getWriter().write("✅ Monthly statement email sent to " + userEmail);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("❌ Failed to send statement email: " + e.getMessage());
        }
    }
}
