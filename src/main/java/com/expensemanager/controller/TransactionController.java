package com.expensemanager.controller;

import com.expensemanager.model.Account;
import com.expensemanager.model.Category;
import com.expensemanager.model.Transaction;
import com.expensemanager.service.TransactionService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpSession;

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
            UUID userId = UUID.fromString("67b78d51-4eec-491c-bbf0-30e982def9e0");

        }
        UUID userId = UUID.fromString("67b78d51-4eec-491c-bbf0-30e982def9e0");
        String action = request.getParameter("action");
        String idParam = request.getParameter("id");

        if ("edit".equals(action) && idParam != null) {
            UUID transactionId = UUID.fromString(idParam);

            Transaction editTransaction = transactionService.getTransactionById(transactionId);

            request.setAttribute("editTransaction", editTransaction);
        }

        //UUID userId = (UUID) session.getAttribute("user_id");

        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();

        List<Transaction> transList = transactionService.getAllTransactionsByMonthAndYear(userId, month, year);
        List<Category> categoryList = transactionService.getAllCategory(userId);
        List<Account> accountList = transactionService.getAllAccountByUserId(userId);
        request.setAttribute("transList", transList);
        request.setAttribute("categoryList", categoryList);
        request.setAttribute("accountList", accountList);
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
//            response.sendRedirect("login");
//            return;
            UUID userId = UUID.fromString("67b78d51-4eec-491c-bbf0-30e982def9e0");
        }
        //UUID userId = (UUID) session.getAttribute("user_id");
        UUID userId = UUID.fromString("67b78d51-4eec-491c-bbf0-30e982def9e0");
        //FILTER
        if ("filter".equals(action)) {
            String fromDate = request.getParameter("fromDate");
            String toDate = request.getParameter("toDate");
            String notes = request.getParameter("notes");
            String type = request.getParameter("type");
            List<Transaction>transactionList = transactionService.filterPanel(userId, fromDate, toDate, notes, type);

            // Format tháng + năm
            String dateRangeLabel;
            if (fromDate != null && !fromDate.isEmpty() && toDate != null && !toDate.isEmpty()) {
                LocalDate from = LocalDate.parse(fromDate);
                LocalDate to = LocalDate.parse(toDate);
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM yyyy"); // ví dụ: Oct 2025
                dateRangeLabel = fmt.format(from) + " → " + fmt.format(to);
            } else {
                // Nếu không filter thì nó hiện tháng/năm hiện tại
                LocalDate now = LocalDate.now();
                dateRangeLabel = now.format(DateTimeFormatter.ofPattern("MMM yyyy"));
            }

            request.setAttribute("dateRangeLabel", dateRangeLabel);
            request.setAttribute("transList", transactionList);
            request.setAttribute("view", "/views/transaction.jsp");
            request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
        }
        //ADD_INCOME
        else if ("add_income".equals(action)) {
            String categoryId = request.getParameter("category");
            String accountId = request.getParameter("account");
            String amount = request.getParameter("value");
            String note = request.getParameter("notes");
            String transactionDate = request.getParameter("date");
            String time = request.getParameter("time");
            String type = request.getParameter("type");

            transactionService.addIncomeTransaction(categoryId, accountId, amount, note, transactionDate, time, type, userId);
            response.sendRedirect(request.getContextPath() + "/transaction");
        }

        //ADD_EXPENSE
        else if ("add_expense".equals(action)) {
            String categoryId = request.getParameter("category");
            String accountId = request.getParameter("account");
            String amount = request.getParameter("value");
            String note = request.getParameter("notes");
            String transactionDate = request.getParameter("date");
            String time = request.getParameter("time");
            String type = request.getParameter("type");

            transactionService.addExpenseTransaction(categoryId, accountId, amount, note, transactionDate, time, type, userId);
            response.sendRedirect(request.getContextPath() + "/transaction");
        }

        //UPDATE_TRANSACTION
        else if ("update_income".equals(action) || "update_expense".equals(action)) {
            String id = request.getParameter("id");
            String categoryId = request.getParameter("category");
            String accountId = request.getParameter("account");
            String amount = request.getParameter("value");
            String note = request.getParameter("notes");
            String date = request.getParameter("date");
            String time = request.getParameter("time");
            String type = request.getParameter("type");

            transactionService.updateTransaction(id, categoryId, accountId, amount, note, date, time, type, userId);
        }
    }
}
