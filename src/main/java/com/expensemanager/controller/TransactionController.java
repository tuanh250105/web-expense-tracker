package com.expensemanager.controller;

import com.expensemanager.model.Account;
import com.expensemanager.model.Category;
import com.expensemanager.model.Transaction;
import com.expensemanager.model.User;
import com.expensemanager.service.AccountService;
import com.expensemanager.service.TransactionService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@WebServlet("/transaction")
public class TransactionController extends HttpServlet {
    private final TransactionService transactionService = new TransactionService();
    private final AccountService accountService = new AccountService();
    private static final String transaction_cache = "transactionCache";
    private static final int cache_size = 6;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");
        UUID userId;
        if (user != null) {
            userId = user.getId();
        }else{
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }


        String action = request.getParameter("action");
        String transactionId = request.getParameter("transactionId");

        // Handle edit action
        if ("edit".equals(action) && transactionId != null) {
            UUID transId = UUID.fromString(transactionId);
            Transaction editTransaction = transactionService.getTransactionById(transId);
            request.setAttribute("editTransaction", editTransaction);
        }


        String monthParam = request.getParameter("month");
        String yearParam = request.getParameter("year");
        String navigate = request.getParameter("navigate");

        YearMonth currentYearMonth;

        if (navigate != null) {
            YearMonth sessionYearMonth = (YearMonth) session.getAttribute("currentYearMonth");
            if (sessionYearMonth == null) {
                sessionYearMonth = YearMonth.now();
            }

            if ("prev".equals(navigate)) {
                currentYearMonth = sessionYearMonth.minusMonths(1);
            } else if ("next".equals(navigate)) {
                currentYearMonth = sessionYearMonth.plusMonths(1);
            } else {
                currentYearMonth = sessionYearMonth;
            }
        } else if (monthParam != null && yearParam != null) {
            int month = Integer.parseInt(monthParam);
            int year = Integer.parseInt(yearParam);
            currentYearMonth = YearMonth.of(year, month);
        } else {
            currentYearMonth = YearMonth.now();
        }

        session.setAttribute("currentYearMonth", currentYearMonth);

        Map<String, List<Transaction>> cache = getOrCreateCache(session);
        String cacheKey = getCacheKey(currentYearMonth);
        List<Transaction> transList = cache.get(cacheKey);

        if (transList == null) {
            transList = transactionService.getAllTransactionsByMonthAndYear(
                    userId,
                    currentYearMonth.getMonthValue(),
                    currentYearMonth.getYear()
            );
            cache.put(cacheKey, transList);
            preloadAdjacentMonths(userId, currentYearMonth, cache);
            cleanupCache(cache);
        }

        List<Category> categoryList = getCategoryList(session, userId);
        List<Account> accountList = getAccountList(session, userId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);
        String dateRangeLabel = currentYearMonth.format(formatter);

        YearMonth currentMonth = YearMonth.now();

        BigDecimal totalIncome = transList.stream()
                .filter(t -> "income".equalsIgnoreCase(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = transList.stream()
                .filter(t -> "expense".equalsIgnoreCase(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalBalance = totalIncome.subtract(totalExpense);

        request.setAttribute("pageJs", "transaction.js");
        request.setAttribute("pageCss", "transaction.css");
        request.setAttribute("transList", transList);
        request.setAttribute("categoryList", categoryList);
        request.setAttribute("accountList", accountList);
        request.setAttribute("dateRangeLabel", dateRangeLabel);
        request.setAttribute("currentYearMonth", currentYearMonth);
        request.setAttribute("isCurrentMonth", currentYearMonth.equals(currentMonth));
        request.setAttribute("totalIncome", totalIncome);
        request.setAttribute("totalExpense", totalExpense);
        request.setAttribute("totalBalance", totalBalance);

        request.setAttribute("view", "/views/transaction.jsp");
        request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");
        UUID user_Id = user.getId();

        UUID userId;
        if (session == null || session.getAttribute("user_id") == null) {
            userId = UUID.fromString("67b78d51-4eec-491c-bbf0-30e982def9e0");
        } else {
            userId = user_Id;
        }

        if ("filter".equals(action)) {
            String fromDate = request.getParameter("fromDate");
            String toDate = request.getParameter("toDate");
            String notes = request.getParameter("notes");
            String[] typeValues = request.getParameterValues("type");

            List<Transaction> transactionList = transactionService.filterPanel(userId, fromDate, toDate, notes, typeValues);

            String dateRangeLabel;
            if (fromDate != null && !fromDate.isEmpty() && toDate != null && !toDate.isEmpty()) {
                LocalDate from = LocalDate.parse(fromDate);
                LocalDate to = LocalDate.parse(toDate);
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM yyyy");
                dateRangeLabel = fmt.format(from) + " → " + fmt.format(to);
            } else {
                LocalDate now = LocalDate.now();
                dateRangeLabel = now.format(DateTimeFormatter.ofPattern("MMM yyyy"));
            }

            List<Category> categoryList = getCategoryList(session, userId);
            List<Account> accountList = getAccountList(session, userId);

            request.setAttribute("pageJs", "transaction.js");
            request.setAttribute("pageCss", "transaction.css");
            request.setAttribute("dateRangeLabel", dateRangeLabel);
            request.setAttribute("transList", transactionList);
            request.setAttribute("categoryList", categoryList);
            request.setAttribute("accountList", accountList);
            request.setAttribute("isFiltered", true);
            request.setAttribute("view", "/views/transaction.jsp");
            request.getRequestDispatcher("/layout/layout.jsp").forward(request, response);
            return;
        }
        else if ("add_income".equals(action)) {
            String categoryId = request.getParameter("category");
            String accountId = request.getParameter("account");
            String amount = request.getParameter("value");
            String note = request.getParameter("notes");
            String transactionDate = request.getParameter("date");
            String time = request.getParameter("time");
            String type = request.getParameter("type");

            transactionService.addIncomeTransaction(categoryId, accountId, amount, note, transactionDate, time, type, userId);
            clearTransactionCache(session);
            response.sendRedirect(request.getContextPath() + "/transaction");
        }
        else if ("add_expense".equals(action)) {
            String categoryId = request.getParameter("category");
            String accountId = request.getParameter("account");
            String amount = request.getParameter("value");
            String note = request.getParameter("notes");
            String transactionDate = request.getParameter("date");
            String time = request.getParameter("time");
            String type = request.getParameter("type");

            transactionService.addExpenseTransaction(categoryId, accountId, amount, note, transactionDate, time, type, userId);
            clearTransactionCache(session);
            response.sendRedirect(request.getContextPath() + "/transaction");
        }
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
            clearTransactionCache(session);
            response.sendRedirect(request.getContextPath() + "/transaction");
        }
        else if ("delete".equals(action)) {
            String id = request.getParameter("id");
            transactionService.deleteTransaction(id);
            clearTransactionCache(session);
            response.sendRedirect(request.getContextPath() + "/transaction");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<Transaction>> getOrCreateCache(HttpSession session) {
        Map<String, List<Transaction>> cache =
                (Map<String, List<Transaction>>) session.getAttribute(transaction_cache);

        if (cache == null) {
            cache = new LinkedHashMap<>(cache_size + 1, 0.75f, true);
            session.setAttribute(transaction_cache, cache);
        }

        return cache;
    }

    private String getCacheKey(YearMonth yearMonth) {
        return yearMonth.getYear() + "-" + yearMonth.getMonthValue();
    }

    private void preloadAdjacentMonths(UUID userId, YearMonth current, Map<String, List<Transaction>> cache) {
        YearMonth[] toPreload = {
                current.minusMonths(2),
                current.minusMonths(1),
                current.plusMonths(1)
        };

        for (YearMonth ym : toPreload) {
            String key = getCacheKey(ym);
            if (!cache.containsKey(key)) {
                List<Transaction> transactions = transactionService.getAllTransactionsByMonthAndYear(
                        userId,
                        ym.getMonthValue(),
                        ym.getYear()
                );
                cache.put(key, transactions);
            }
        }
    }

    private void cleanupCache(Map<String, List<Transaction>> cache) {
        if (cache.size() > cache_size) {
            Iterator<String> iterator = cache.keySet().iterator();
            while (cache.size() > cache_size && iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }
        }
    }

    private void clearTransactionCache(HttpSession session) {
        if (session != null) {
            session.removeAttribute(transaction_cache);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Category> getCategoryList(HttpSession session, UUID userId) {
        List<Category> categoryList = (List<Category>) session.getAttribute("categoryList");
        if (categoryList == null) {
            categoryList = transactionService.getAllCategory(userId);
            session.setAttribute("categoryList", categoryList);
        }
        return categoryList;
    }

    @SuppressWarnings("unchecked")
    private List<Account> getAccountList(HttpSession session, UUID userId) {
        List<Account> accountList = (List<Account>) session.getAttribute("accountList");
        if (accountList == null) {
            accountList = accountService.getAccountsByUser(userId);
            //accountList = transactionService.getAllAccountByUserId(userId);
            session.setAttribute("accountList", accountList);
        }
        return accountList;
    }
}