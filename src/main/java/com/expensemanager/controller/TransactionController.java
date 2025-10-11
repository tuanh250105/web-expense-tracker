package com.expensemanager.controller;

import com.expensemanager.model.Transaction;
import com.expensemanager.service.TransactionService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpSession;
import org.hibernate.jdbc.Expectation;


@WebServlet("/transaction")
public class TransactionController extends HttpServlet {
    private final TransactionService transactionService = new TransactionService();


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
//            response.sendRedirect("login");
//            return;
            UUID userId = UUID.fromString("4efcb554-f4c5-442a-b57c-89d213861501");

        }
        UUID userId = UUID.fromString("4efcb554-f4c5-442a-b57c-89d213861501");
        //UUID userId = (UUID) session.getAttribute("user_id");

        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();

        List<Transaction> transList = transactionService.getAllTransactionsByMonthAndYear(userId, month, year);

        request.setAttribute("transList", transList);
        request.setAttribute("view", "/views/transaction.jsp");
        request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        //Lấy action
        String action = request.getParameter("action");

        //Lấy User hiện tại
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            response.sendRedirect("login");
            return;
        }
        UUID userId = (UUID) session.getAttribute("user_id");

        //FILTER
        if ("filter".equals(action)) {
            String fromDate = request.getParameter("fromDate");
            String toDate = request.getParameter("toDate");
            String notes = request.getParameter("notes");
            String type = request.getParameter("type");
            //transactionService.filter(userId, fromDate, toDate, notes, type);
        }
        //ADD_INCOME
        else if ("add_income".equals(action)) {
            String categoryId = request.getParameter("categoryId");
            String accountId = request.getParameter("account");
            String amount = request.getParameter("value");
            String note = request.getParameter("notes");
            String transactionDate = request.getParameter("transactionDate");

        }
    }
}
