package com.expensemanager.service;

import com.expensemanager.dao.TransactionDAO;
import com.expensemanager.model.Account;
import com.expensemanager.model.Category;
import com.expensemanager.model.Transaction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;


public class TransactionService {
    private TransactionDAO transactionDAO = new TransactionDAO();

    public List<Transaction> getAllTransactionsByMonthAndYear(UUID userId, int month, int year) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().plusDays(1).atStartOfDay(); //2025-11-01 00:00:00

        return transactionDAO.getAllTransactionsByMonthAndYear(userId, startOfMonth, endOfMonth);
    }

    public void addIncomeTransaction(String categoryId, String accountId, String amount, String note, String transactionDate, String time, String type, UUID userId) {
        Account account = transactionDAO.findAccountById(UUID.fromString(accountId));
        if (account == null || !account.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Tài khoản không tồn tại hoặc không thuộc về user hiện tại !!!!!!!!!!!!");
        }

        Category category = transactionDAO.findCategoryById(UUID.fromString(categoryId));
        if (category == null) {
            throw new IllegalArgumentException("Category không hợp lệ");
        }

        Transaction t = new Transaction();
        t.setAccount(account);
        t.setCategory(category);
        t.setAmount(Integer.parseInt(amount));
        t.setNote(note);
        t.setTransactionDate(LocalDateTime.parse(transactionDate + "T" + time));
        t.setType(type);

        transactionDAO.addIncomeTransaction(t);
    }

    public void addExpenseTransaction(String categoryId, String accountId, String amount, String note, String transactionDate, String time, String type, UUID userId) {
        Account account = transactionDAO.findAccountById(UUID.fromString(accountId));
        if (account == null || !account.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Tài khoản không tồn tại hoặc không thuộc về user hiện tại");
        }

        Category category = transactionDAO.findCategoryById(UUID.fromString(categoryId));
        if (category == null) {
            throw new IllegalArgumentException("Category không hợp lệ");
        }

        Transaction t = new Transaction();
        t.setAccount(account);
        t.setCategory(category);
        t.setAmount(Integer.parseInt(amount));
        t.setNote(note);
        t.setTransactionDate(LocalDateTime.parse(transactionDate + "T" + time));
        t.setType(type);

        transactionDAO.addExpenseTransaction(t);
    }

    public List<Category> getAllCategory(UUID userId){
         List<Category> category = transactionDAO.findAllCategoryOfUser(userId);
         return category;
    }

    public Transaction getTransactionById(UUID transactionId) {
        return transactionDAO.getTransactionById(transactionId);
    }

    public List<Account> getAllAccountByUserId(UUID userId) {
        return transactionDAO.getAllAccountByUserId(userId);
    }

    public void updateTransaction(String id, String categoryId, String accountId, String amount, String note, String date, String time, String type, UUID userId) {
        String transactionDate = date + "T" + time;
        Account account = transactionDAO.findAccountById(UUID.fromString(accountId));
        if (account == null || !account.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Tài khoản không tồn tại hoặc không thuộc về user hiện tại");
        }

        Category category = transactionDAO.findCategoryById(UUID.fromString(categoryId));
        if (category == null) {
            throw new IllegalArgumentException("Category không hợp lệ");
        }

        Transaction t = new Transaction();
        t.setId(UUID.fromString(id));
        t.setAccount(account);
        t.setCategory(category);
        t.setAmount(Integer.parseInt(amount));
        t.setNote(note);
        t.setTransactionDate(LocalDateTime.parse(transactionDate));
        t.setType(type);

        transactionDAO.updateTransaction(t);
    }

    public List<Transaction> filterPanel(UUID userId, String fromDate, String toDate, String notes, String type) {
        if (fromDate != null && !fromDate.isEmpty() && toDate != null && !toDate.isEmpty()) {
            if (LocalDate.parse(fromDate).isAfter(LocalDate.parse(toDate))) {
                throw new IllegalArgumentException("From date cannot be after to date");
            }
        }

        // Chuyển chuỗi rỗng thành null cho DAO xử lý dễ hơn
        fromDate = (fromDate == null || fromDate.isEmpty()) ? null : fromDate;
        toDate = (toDate == null || toDate.isEmpty()) ? null : toDate;
        notes = (notes == null) ? "" : notes;
        type = (type == null) ? "" : type;

        return transactionDAO.filter(userId, fromDate, toDate, notes, type);
    }
}

