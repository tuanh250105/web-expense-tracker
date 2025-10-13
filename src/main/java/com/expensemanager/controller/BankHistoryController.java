package com.expensemanager.controller;

import com.expensemanager.model.Account;
import com.expensemanager.model.Category;
import com.expensemanager.model.Transaction;
import com.expensemanager.model.User;
import com.expensemanager.service.AccountService;
import com.expensemanager.service.CategoryService;
import com.expensemanager.service.TransactionServicestart;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/api/bank-history/*")
public class BankHistoryController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(BankHistoryController.class.getName());
    private final TransactionServicestart transactionService = new TransactionServicestart();
    private final AccountService accountService = new AccountService();
    private final CategoryService categoryService = new CategoryService(); // Added for category handling
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (isApiCall(request)) {
            handleApiRequest(request, response);
        } else {
            handlePageRequest(request, response);
        }
    }

    private void handlePageRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("delete".equals(action)) {
            try {
                UUID transactionId = UUID.fromString(request.getParameter("id"));
                // Note: Security check should be done in the service layer to ensure the user owns this transaction
                transactionService.deleteTransaction(transactionId);
                response.sendRedirect(request.getContextPath() + "/transactions"); // Redirect to a general transactions page
                return;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error deleting transaction", e);
                request.setAttribute("error", "Could not delete the transaction.");
            }
        }

        loadTransactionData(request);
        request.setAttribute("view", "/views/transactions.jsp");
        request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
    }

    private void handleApiRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            UUID userId = getUserIdFromSession(request);
            String accountIdFilter = request.getParameter("accountId");

            List<Transaction> transactions = transactionService.getFilteredTransactions(userId, accountIdFilter);
            response.getWriter().write(gson.toJson(Map.of("success", true, "data", transactions)));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in API request", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(Map.of("success", false, "error", e.getMessage())));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            UUID userId = getUserIdFromSession(request);
            String accountIdStr = request.getParameter("accountId");
            String categoryIdStr = request.getParameter("categoryId");
            String note = request.getParameter("note");
            String amountStr = request.getParameter("amount");
            String type = request.getParameter("type");

            Transaction newTransaction = new Transaction();
            newTransaction.setNote(note);
            newTransaction.setType(type);

            if (amountStr != null && !amountStr.isEmpty()) {
                newTransaction.setAmount(new BigDecimal(amountStr));
            }

            if (accountIdStr != null && !accountIdStr.isEmpty()) {
                Account account = accountService.getAccountById(UUID.fromString(accountIdStr));
                // Security check: ensure account belongs to user
                if (account != null && account.getUser().getId().equals(userId)) {
                    newTransaction.setAccount(account);
                }
            }

            if (categoryIdStr != null && !categoryIdStr.isEmpty()) {
                Category category = categoryService.getCategoryById(UUID.fromString(categoryIdStr));
                // Security check: ensure category belongs to user
                if (category != null && category.getUser().getId().equals(userId)) {
                    newTransaction.setCategory(category);
                }
            }

            transactionService.addTransaction(newTransaction);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding transaction", e);
            request.setAttribute("error", "Error occurred while adding the transaction.");
            loadTransactionData(request);
            request.setAttribute("view", "/views/transactions.jsp");
            request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/transactions");
    }

    private void loadTransactionData(HttpServletRequest request) {
        try {
            UUID userId = getUserIdFromSession(request);
            String accountIdFilter = request.getParameter("filterAccountId");

            List<Transaction> transactions = transactionService.getFilteredTransactions(userId, accountIdFilter);
            List<Account> accounts = accountService.getAccountsByUser(userId);

            request.setAttribute("transactions", transactions);
            request.setAttribute("accounts", accounts);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading transaction data", e);
            request.setAttribute("error", "Could not load transaction data.");
        }
    }

    private boolean isApiCall(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        String acceptHeader = request.getHeader("Accept");
        return (acceptHeader != null && acceptHeader.contains("application/json")) || (pathInfo != null && !pathInfo.equals("/"));
    }

    private UUID getUserIdFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object userObj = session.getAttribute("user");
            if (userObj instanceof User) {
                return ((User) userObj).getId();
            }
        }
        // Fallback for development or if session structure is different
        return UUID.fromString("67b78d51-4eec-491c-bbf0-30e982def9e0");
    }
}
