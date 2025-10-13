package com.expensemanager.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {
	public boolean send(String to, String subject, String html) {
		try {
			Properties props = new Properties();
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", System.getenv("SMTP_HOST"));
			props.put("mail.smtp.port", System.getenv("SMTP_PORT"));

			Session session = Session.getInstance(props, new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(
						System.getenv("SMTP_USER"), System.getenv("SMTP_PASS"));
				}
			});

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(System.getenv("SMTP_USER")));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			message.setSubject(subject);
			message.setContent(html, "text/html; charset=UTF-8");
			Transport.send(message);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}