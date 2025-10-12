package com.expensemanager.service;

import com.expensemanager.dao.ImportExportDAO;
import com.expensemanager.model.Account;
import com.expensemanager.model.Category;
import com.expensemanager.model.Transaction;
import com.expensemanager.util.CSVUtil;
import com.expensemanager.util.JPAUtil;
import com.expensemanager.util.XLSXUtil;
import com.expensemanager.util.PDFUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class ImportExportService {

    private final ImportExportDAO dao;
    private final AccountService accountService;
    private final EntityManagerFactory emf = JPAUtil.getEntityManagerFactory();

    public ImportExportService() {
        dao = new ImportExportDAO();
        accountService = new AccountService();
    }

    /**
     * Đọc file (CSV hoặc XLSX), gán account và category thật từ DB.
     * Không cần thay đổi model Transaction.
     */
    public List<Transaction> previewImport(InputStream file, String type, UUID accountId) throws Exception {
        List<Transaction> list;

        // ✅ Dữ liệu đọc raw từng dòng để lấy category_id riêng
        List<Map<String, String>> rawData;
        if ("csv".equalsIgnoreCase(type)) {
            rawData = CSVUtil.readRawCSV(file);
        } else if ("xlsx".equalsIgnoreCase(type)) {
            rawData = XLSXUtil.readRawXLSX(file);
        } else {
            throw new IllegalArgumentException("Định dạng file không hỗ trợ: " + type);
        }

        EntityManager em = emf.createEntityManager();
        Account acc = accountService.findById(accountId);
        if (acc == null) throw new IllegalArgumentException("Tài khoản không tồn tại: " + accountId);

        List<Transaction> result = new ArrayList<>();

        for (Map<String, String> row : rawData) {
            try {
                Transaction t = new Transaction();

                // gán các cột cơ bản
                t.setType(row.get("type"));
                t.setAmount(Integer.parseInt(row.get("amount")));
                t.setNote(row.get("note"));
                t.setTransactionDate(java.time.LocalDateTime.parse(row.get("transaction_date")));
                t.setCreate_at(java.time.LocalDateTime.parse(row.get("create_at")));
                t.setUpdate_at(java.time.LocalDateTime.parse(row.get("update_at")));
                t.setAccount(acc);

                // ✅ Lấy category_id rồi fetch Category từ DB
                String catIdStr = row.get("category_id");
                if (catIdStr == null || catIdStr.isBlank()) {
                    throw new IllegalArgumentException("Thiếu category_id trong dữ liệu.");
                }

                try {
                    int catId = Integer.parseInt(catIdStr.trim());
                    Category c = em.find(Category.class, catId);
                    if (c == null) {
                        throw new IllegalArgumentException("Category ID " + catId + " không tồn tại trong DB.");
                    }
                    t.setCategory(c);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("category_id không hợp lệ: " + catIdStr);
                }

                result.add(t);
            } catch (Exception e) {
                throw new IllegalArgumentException("Lỗi khi đọc dòng dữ liệu: " + e.getMessage());
            }
        }

        em.close();
        return result;
    }

    public void saveTransactions(List<Transaction> transactions) {
        if (transactions != null && !transactions.isEmpty()) {
            dao.saveTransactions(transactions);
        }
    }

    public byte[] exportByAccount(UUID accountId, String start, String end, String format) throws Exception {
        LocalDate startDate;
        LocalDate endDate;

        try {
            startDate = (start == null || start.isBlank()) ? LocalDate.MIN : LocalDate.parse(start);
        } catch (Exception e) {
            startDate = LocalDate.MIN;
        }
        try {
            endDate = (end == null || end.isBlank()) ? LocalDate.now() : LocalDate.parse(end);
        } catch (Exception e) {
            endDate = LocalDate.now();
        }

        List<Transaction> data = dao.getTransactionsByAccountAndDate(accountId, startDate, endDate);
        if (data == null) data = Collections.emptyList();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        switch (format.toLowerCase()) {
            case "csv" -> CSVUtil.writeCSV(out, data);
            case "xlsx" -> XLSXUtil.writeXLSX(out, data);
            case "pdf" -> PDFUtil.writePDF(out, data);
            default -> throw new IllegalArgumentException("Định dạng không hỗ trợ: " + format);
        }
        return out.toByteArray();
    }

    public void writeExportResponse(HttpServletResponse resp, String format, byte[] data) throws Exception {
        switch (format.toLowerCase()) {
            case "csv" -> {
                resp.setContentType("text/csv");
                resp.setHeader("Content-Disposition", "attachment; filename=transactions.csv");
            }
            case "xlsx" -> {
                resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                resp.setHeader("Content-Disposition", "attachment; filename=transactions.xlsx");
            }
            case "pdf" -> {
                resp.setContentType("application/pdf");
                resp.setHeader("Content-Disposition", "attachment; filename=transactions.pdf");
            }
            default -> throw new IllegalArgumentException("Định dạng không hỗ trợ: " + format);
        }

        var os = resp.getOutputStream();
        os.write(data);
        os.flush();
    }
}
