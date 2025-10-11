package com.expensemanager.service;

import com.expensemanager.dao.TransactionRepository;
import com.expensemanager.model.Transaction;

import java.time.OffsetDateTime;
import java.util.List;

public class AnalyticsService {
    private final TransactionRepository repo = new TransactionRepository();
    public List<Transaction> find(String type, OffsetDateTime from, OffsetDateTime to) {
        return repo.find(type, from, to);
    }
}
