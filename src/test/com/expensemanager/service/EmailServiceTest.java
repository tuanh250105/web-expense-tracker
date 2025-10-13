package src.test.com.expensemanager.service;

import org.junit.jupiter.api.Test;

public class EmailServiceTest {

    @Test
    void testSendEmail() {
        EmailService emailService = new EmailService();
        String html = "<h1>Hello from BudgetBuddy</h1><p>This is a test email.</p>";
        emailService.sendEmail("test@user.com", "✅ BudgetBuddy Test Email", html);
    }
}
