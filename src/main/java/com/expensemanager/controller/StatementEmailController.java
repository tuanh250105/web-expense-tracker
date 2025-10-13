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

        String userEmail = "test@user.com";
        String username = "Test User";

        UUID userUUID;

        try {
            // Try to load real UserDAO (only if available in this branch)
            Class<?> userDAOClass = Class.forName("com.expensemanager.dao.UserDAO");
            Object userDAO = userDAOClass.getDeclaredConstructor().newInstance();

            var findByEmailMethod = userDAOClass.getMethod("findByEmail", String.class);
            Object user = findByEmailMethod.invoke(userDAO, userEmail);
            var getIdMethod = user.getClass().getMethod("getId");
            userUUID = (UUID) getIdMethod.invoke(user);

            System.out.println("[StatementEmailController] ✅ Loaded real user UUID: " + userUUID);

        } catch (ClassNotFoundException e) {
            // Fallback if UserDAO doesn't exist in this branch
            userUUID = UUID.fromString("6b4d3a2e-baa5-4b5e-9d3a-8a9cc27b4ad3");
            System.out.println("[StatementEmailController] ⚠️ UserDAO not found — using fallback UUID: " + userUUID);
        } catch (Exception e) {
            // Any reflection or DAO issues also use fallback
            userUUID = UUID.fromString("6b4d3a2e-baa5-4b5e-9d3a-8a9cc27b4ad3");
            System.out.println("[StatementEmailController] ⚠️ Failed to fetch real user, using fallback UUID: " + userUUID);
            e.printStackTrace();
        }

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
