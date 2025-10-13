package com.expensemanager.service;

import java.util.List;
import java.util.UUID;

import com.expensemanager.dao.AccountDAO;
import com.expensemanager.model.Account;

/**
 * AccountService - Business logic cho Account
 */
public class AccountService {

    private final AccountDAO accountDAO = new AccountDAO();

    public List<Account> getAllAccounts() {
        return accountDAO.findAll();
    }

    public List<Account> getAccountsByUser(UUID userId) {
        return accountDAO.findAllByUser(userId);
    }

    public Account getAccountById(UUID id) {
        return accountDAO.findById(id);
    }

    public void addAccount(Account account) {
        accountDAO.save(account);
    }

    public void updateAccount(Account account) {
        accountDAO.update(account);
    }

    public void deleteAccount(UUID id) {
        accountDAO.delete(id);
    }
}
