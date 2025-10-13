package com.expensemanager.controller;

import com.expensemanager.model.FAQ;
import com.expensemanager.service.FAQService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@WebServlet("/faq")
public class FAQController extends HttpServlet {
    private FAQService service = new FAQService();

    @Override
    public void init() {
        // Gọi initSampleFAQs một lần để thêm dữ liệu mẫu (nếu DB rỗng)
        // Comment lại sau khi chạy lần đầu
        //service.initSampleFAQs();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<FAQ> faqs = service.getAllFAQs();
        request.setAttribute("faqs", faqs);
        request.setAttribute("view", "/views/faq.jsp");
        request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        // pht triển theem khi login là admin thì mói thêm nhóe
        if ("add".equals(action)) {
            String question = request.getParameter("question");
            String answer = request.getParameter("answer"); // answer có thể chứa HTML
            FAQ faq = new FAQ(question, answer);
            service.addFAQ(faq);
            response.sendRedirect("/faq");
        } else if ("delete".equals(action)) {
            UUID id = UUID.fromString(request.getParameter("id"));
            service.deleteFAQ(id);
            response.sendRedirect("/faq");
        }
    }
}