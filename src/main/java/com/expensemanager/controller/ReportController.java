package com.expensemanager.controller;


import com.expensemanager.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.servlet.http.HttpSession;

@WebServlet("/report")
public class ReportController extends HttpServlet {


    private static final String smtp_host = "smtp.gmail.com";
    private static final String smtp_port = "587";
    private static final String sender_email = "tuanh25012005@gmail.com"; // Cho moi User sai chung 1 Email =)))
    private static final String sender_pass = System.getenv("SMTP_PASS");
    private static final String receiver = "vominhkhoi299@gmail.com"; // Nay Admin nha

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/views/report.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String senderName = request.getParameter("senderName");
        String senderEmail = request.getParameter("senderEmail");
        String subject = request.getParameter("subject");
        String message = request.getParameter("message");

        if (senderName == null || senderEmail == null || subject == null || message == null || senderName.trim().isEmpty() || senderEmail.trim().isEmpty() ||
                subject.trim().isEmpty() || message.trim().length() < 10) {
            request.setAttribute("error", "Vui lòng điền đầy đủ thông tin hợp lệ");
            request.getRequestDispatcher("/views/report.jsp").forward(request, response);
            return;
        }
        HttpSession session = request.getSession();
        User user  = (User) session.getAttribute("user");
        UUID userId = user.getId();
        // Send mail đi
        boolean emailSent = sendEmail(senderName, senderEmail, subject, message, userId);

        if (emailSent) {
            response.sendRedirect(request.getContextPath() + "/faq");
        } else {
            request.setAttribute("error", "Có lỗi xảy ra khi gửi email. Vui lòng thử lại sau.");
            request.getRequestDispatcher("/views/report.jsp").forward(request, response);
        }
    }

    private boolean sendEmail(String senderName, String senderEmail, String subject, String message, UUID userId) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", smtp_host);
            props.put("mail.smtp.port", smtp_port);
            props.put("mail.smtp.ssl.trust", smtp_host);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {return new PasswordAuthentication(sender_email, sender_pass);}
            });

            Message mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(sender_email));
            mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiver));
            mimeMessage.setSubject("[Báo Cáo] " + subject);

            String emailContent = String.format(
                    "<html>" +
                            "<body style='font-family: Arial, sans-serif;'>" +
                            "<h2 style='color: #4CAF50;'>Báo Cáo Mới Từ Website</h2>" +
                            "<div style='background: #f5f5f5; padding: 20px; border-radius: 5px;'>" +
                            "<p><strong>Người gửi:</strong> %s</p>" +
                            "<p><strong>Email:</strong> %s</p>" +
                            "<p><strong>Tiêu đề:</strong> %s</p>" +
                            "<hr style='border: 1px solid #ddd;'>" +
                            "<p><strong>Nội dung:</strong></p>" +
                            "<p style='white-space: pre-wrap;'>%s</p>" +
                            "</div>" +
                            "<br>" +
                            "<p style='color: #999; font-size: 12px;'>Email này được gửi từ hệ thống báo cáo trên website.</p>" +
                            "</body>" +
                            "</html>",
                    escapeHtml(senderName),
                    escapeHtml(senderEmail),
                    escapeHtml(subject),
                    escapeHtml(message)
            );

            mimeMessage.setContent(emailContent, "text/html; charset=UTF-8");
            Transport.send(mimeMessage);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Helper method để escape HTML
    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}
