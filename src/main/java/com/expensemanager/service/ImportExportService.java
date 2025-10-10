package com.expensemanager.service;

import com.expensemanager.model.Category;
import com.expensemanager.util.CSVUtil;
import com.expensemanager.util.XLSXUtil;  // Thêm mới cho XLSX

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public class ImportExportService {

    private final TransactionDAO dao = new TransactionDAO();

    // Preview import: Đọc file, gợi ý category, return list cho JSP
    public List<Transaction> previewImport(String userId, InputStream fileStream, UUID accountId) throws Exception {
        List<Transaction> txs = CSVUtil.readTransactions(fileStream);  // Hoặc XLSX nếu detect
        Account account = new Account();
        account.setId(accountId);

        for (Transaction tx : txs) {
            tx.setAccount(account);
            Category suggested = suggestCategory(tx.getNote());
            tx.setCategory(suggested);
            tx.setType(tx.getAmount().compareTo(BigDecimal.ZERO) < 0 ? "expense" : "income");
        }
        return txs;
    }

    // Confirm: Lưu list từ preview
    public void confirmImport(List<Transaction> txs) {
        dao.saveTransactions(txs);  // Giả lập, sau dùng DAO insertBatch với DB conn
    }

    // Export: Filter và write file
    public byte[] exportTransactions(String userId, UUID accountId, Timestamp start, Timestamp end, String format) throws Exception {
        // Query filter từ DAO (giả lập)
        List<Transaction> txs = dao.getTransactionsByFilter(null, userId, accountId != null ? accountId.toString() : "all", start, end);
        if ("csv".equals(format)) {
            return CSVUtil.writeTransactionsToCSV(txs);
        } else if ("xlsx".equals(format)) {
            return XLSXUtil.writeTransactionsToXLSX(txs);
        }
        throw new IllegalArgumentException("Format không hỗ trợ");
    }

    // Gợi ý category dựa keyword
    private Category suggestCategory(String note) {
        if (note.toUpperCase().contains("GRABFOOD")) {
            return dao.getCategoryByName("Ăn uống");
        } else if (note.toUpperCase().contains("SPOTIFY")) {
            return dao.getCategoryByName("Giải trí");
        }
        return dao.getCategoryByName("Khác");
    }

    // Load accounts cho select in JSP
    public List<Account> getAccounts(String userId) {
        return dao.getAccountsByUser(userId);
    }
}