package com.expensemanager.service;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class EmailService {

    private final Session session;
    private final String from;

    public EmailService() {
        Properties props = new Properties();

        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream("email.properties")) {
            if (in == null) throw new RuntimeException("email.properties not found");
            props.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load email.properties", e);
        }

        this.from = props.getProperty("mail.from");
        this.session = Session.getInstance(props);
    }

    public void sendEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject, StandardCharsets.UTF_8.name());

            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlBody, "text/html; charset=UTF-8");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(htmlPart);
            message.setContent(multipart);

            Transport.send(message);
            System.out.println("📧 Sent email to " + to);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
