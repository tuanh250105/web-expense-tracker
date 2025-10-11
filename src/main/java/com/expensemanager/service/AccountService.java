package com.expensemanager.service;

import com.expensemanager.dao.AccountDAO;
import com.expensemanager.model.Account;
import java.util.List;
import java.util.UUID;

public class AccountService {
    private final AccountDAO dao = new AccountDAO();

    public List<Account> getAllAccounts() {
        return dao.getAllAccounts();
    }

    public Account findById(UUID id) {
        return dao.findById(id);
    }
}
