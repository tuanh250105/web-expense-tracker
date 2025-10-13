package com.expensemanager.controller;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

@WebServlet("/report")
public class ReportController extends HttpServlet {

    // Cấu hình email - THAY ĐỔI THÔNG TIN NÀY
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String SENDER_EMAIL = "your-email@gmail.com"; // Email của bạn
    private static final String SENDER_PASSWORD = "your-app-password"; // App Password của Gmail
    private static final String RECEIVER_EMAIL = "your-email@gmail.com"; // Email nhận báo cáo

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Hiển thị form báo cáo
        request.getRequestDispatcher("/views/report.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // Lấy dữ liệu từ form
        String senderName = request.getParameter("senderName");
        String senderEmail = request.getParameter("senderEmail");
        String subject = request.getParameter("subject");
        String message = request.getParameter("message");

        // Validate
        if (senderName == null || senderEmail == null || subject == null || message == null ||
                senderName.trim().isEmpty() || senderEmail.trim().isEmpty() ||
                subject.trim().isEmpty() || message.trim().length() < 10) {

            request.setAttribute("error", "Vui lòng điền đầy đủ thông tin hợp lệ");
            request.getRequestDispatcher("/views/report.jsp").forward(request, response);
            return;
        }

        // Gửi email
        boolean emailSent = sendEmail(senderName, senderEmail, subject, message);

        if (emailSent) {
            // Chuyển đến trang thank you
            response.sendRedirect(request.getContextPath() + "/report/thank-you");
        } else {
            request.setAttribute("error", "Có lỗi xảy ra khi gửi email. Vui lòng thử lại sau.");
            request.getRequestDispatcher("/views/report.jsp").forward(request, response);
        }
    }

    private boolean sendEmail(String senderName, String senderEmail, String subject, String message) {
        try {
            // Cấu hình properties
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.ssl.trust", SMTP_HOST);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");

            // Tạo session
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
                }
            });

            // Tạo message
            Message mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(SENDER_EMAIL));
            mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(RECEIVER_EMAIL));
            mimeMessage.setSubject("[Báo Cáo] " + subject);

            // Nội dung email với HTML
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

            // Gửi email
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
