package com.expensemanager.service;

import com.expensemanager.dao.TransactionDAO;
import com.expensemanager.model.Transaction;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;


public class StatementEmailService {

    private final TransactionDAO transactionDAO;
    private final EmailService emailService;

    public StatementEmailService(TransactionDAO transactionDAO, EmailService emailService) {
        this.transactionDAO = transactionDAO;
        this.emailService = emailService;
    }

    public void sendMonthlyStatement(String userEmail, String username, UUID userId) {
        try {
            //Define the monthly time range
            LocalDateTime startOfMonth = LocalDate.now()
                    .withDayOfMonth(1)
                    .atStartOfDay();

            LocalDateTime endOfMonth = LocalDate.now()
                    .plusMonths(1)
                    .withDayOfMonth(1)
                    .atStartOfDay();

            //Fetch all transactions in this month
            List<Transaction> transactions =
                    transactionDAO.getAllTransactionsByMonthAndYear(userId, startOfMonth, endOfMonth);

            // 🔍 Debug output (appears in Tomcat console)
            System.out.println("[StatementEmailService] Found " + transactions.size() +
                    " transactions for user " + username + " (" + userId + ")");
            for (Transaction t : transactions) {
                System.out.println(" - " + t.getTransactionDate() + " | " +
                        t.getCategory().getName() + " | " +
                        t.getType() + " | " +
                        t.getAmount());
            }

            //Load HTML template
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream("mail-template/statement.html");
            if (inputStream == null) {
                throw new RuntimeException("Email template not found in resources/mail-template/");
            }

            String template = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            //Prepare transaction rows
            StringBuilder tableRows = new StringBuilder();
            if (transactions.isEmpty()) {
                tableRows.append("<tr><td colspan='4' style='text-align:center;'>No transactions this month 🎉</td></tr>");
            } else {
                for (Transaction t : transactions) {
                    tableRows.append("<tr>")
                            .append("<td>").append(t.getTransactionDate().toLocalDate()).append("</td>")
                            .append("<td>").append(t.getCategory().getName()).append("</td>")
                            .append("<td>").append(t.getType()).append("</td>")
                            .append("<td>").append(t.getAmount()).append("</td>")
                            .append("</tr>");
                }
            }

            //Inject data into template
            template = template.replace("{{USERNAME}}", username);
            template = template.replace("{{MONTH}}", LocalDate.now().getMonth().toString());
            template = template.replace("{{TRANSACTION_COUNT}}", String.valueOf(transactions.size()));
            template = template.replace("{{TRANSACTION_ROWS}}", tableRows.toString());

            //Send email
            emailService.sendEmail(
                    userEmail,
                    "📊 Your Monthly Statement - " + LocalDate.now().getMonth(),
                    template
            );

            System.out.println("[StatementEmailService] ✅ Monthly statement email sent successfully to " + userEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send statement email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}