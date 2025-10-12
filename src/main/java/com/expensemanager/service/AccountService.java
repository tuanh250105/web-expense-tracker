package com.expensemanager.service;

import com.expensemanager.dao.AccountDAO;
import com.expensemanager.model.Account;

import java.util.List;
import java.util.UUID;

public class AccountService {
    private final AccountDAO dao = new AccountDAO();

    // ✅ Lấy tất cả tài khoản của user hiện tại
    public List<Account> getAllAccountsByUser(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID không hợp lệ.");
        }
        return dao.getAllAccountsByUser(userId);
    }

    // ✅ Tìm tài khoản theo ID (có thể kiểm tra quyền user ở Controller)
    public Account findById(UUID id) {
        return dao.findById(id);
    }

    // ✅ Lưu hoặc cập nhật tài khoản — đảm bảo account có userId gắn vào
    public void save(Account account, UUID userId) {
        if (account == null) {
            throw new IllegalArgumentException("Account không được null.");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID không hợp lệ.");
        }

        // Gắn userId vào account (trong trường hợp chưa có)
        if (account.getUser() == null || account.getUser().getId() == null) {
            account.getUser().setId(userId);
        }

        dao.saveOrUpdate(account);
    }

    // ✅ Xoá tài khoản (chỉ khi đúng user)
    public void delete(UUID accountId, UUID userId) {
        if (accountId == null || userId == null) {
            throw new IllegalArgumentException("Account ID hoặc User ID không hợp lệ.");
        }
        dao.delete(accountId, userId);
    }
}
