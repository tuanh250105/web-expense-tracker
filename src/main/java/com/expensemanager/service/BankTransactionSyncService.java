package com.expensemanager.service;

import java.util.List;
import java.util.UUID;

import com.expensemanager.model.Transaction;

/**
 * Service Interface cho việc đồng bộ, import và export Bank Transaction.
 * Sử dụng Transaction entity mapping với bảng transactions
 */
public interface BankTransactionSyncService {

    /**
     * Đồng bộ dữ liệu từ API của ngân hàng.
     */
    int syncFromBankApi(UUID accountId, String bankApiConfig);

    /**
     * Import dữ liệu từ một file (ví dụ: CSV, Excel).
     * @param fileData Dữ liệu file dưới dạng byte array.
     */
    List<Transaction> importFromFile(UUID accountId, byte[] fileData, String originalFilename);

    /**
     * Export dữ liệu ra file.
     * @return Dữ liệu file dưới dạng byte array.
     */
    byte[] exportToFile(UUID accountId, String format);
}