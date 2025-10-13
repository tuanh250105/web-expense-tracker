package com.expensemanager.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    private final String fromEmail = "your_email@example.com";
    private final String password = "your_password";

    // ✅ Core modern email sender
    public void sendEmail(String to, String subject, String htmlBody) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(fromEmail, password);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(htmlBody, "text/html; charset=UTF-8");

            Transport.send(message);
            System.out.println("[EmailService] ✅ Email sent to " + to);

        } catch (Exception e) {
            System.err.println("[EmailService] ❌ Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ✅ Compatibility method for legacy code returning boolean
    public boolean send(String to, String subject, String body) {
        try {
            sendEmail(to, subject, body);
            return true;
        } catch (Exception e) {
            System.err.println("[EmailService] ❌ send() failed: " + e.getMessage());
            return false;
        }
    }
}
