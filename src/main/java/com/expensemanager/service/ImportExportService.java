package com.expensemanager.service;

import com.expensemanager.dao.ImportExportDAO;
import com.expensemanager.model.Account;
import com.expensemanager.model.Category;
import com.expensemanager.model.Transaction;
import com.expensemanager.util.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class ImportExportService {

    private final ImportExportDAO dao;
    private final AccountService accountService;
    private final EntityManagerFactory emf = JpaUtil.getEntityManagerFactory();

    public ImportExportService() {
        dao = new ImportExportDAO();
        accountService = new AccountService();
    }

    /**
     * Đọc file CSV/XLSX và gán account, category thật từ DB
     */
    public List<Transaction> previewImport(InputStream file, String type, UUID accountId) throws Exception {
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
        if (acc == null)
            throw new IllegalArgumentException("Tài khoản không tồn tại: " + accountId);

        List<Transaction> result = new ArrayList<>();

        for (Map<String, String> raw : rawData) {
            // normalize key (bảo vệ khi header không đúng)
            Map<String, String> row = new HashMap<>();
            for (Map.Entry<String, String> e : raw.entrySet()) {
                String k = (e.getKey() == null)
                        ? ""
                        : e.getKey().replace("\uFEFF", "").trim().toLowerCase(Locale.ROOT);
                String v = (e.getValue() == null) ? "" : e.getValue().trim();
                row.put(k, v);
            }

            try {
                Transaction t = new Transaction();

                // Type
                String typeStr = row.getOrDefault("type", "");
                if (!typeStr.equalsIgnoreCase("income") && !typeStr.equalsIgnoreCase("expense"))
                    throw new IllegalArgumentException("type không hợp lệ: " + typeStr);
                t.setType(typeStr);

                // Amount
                String amountStr = row.getOrDefault("amount", "");
                amountStr = amountStr.replaceAll("[^0-9\\-]", "");
                t.setAmount(amountStr.isEmpty() ? 0 : Integer.parseInt(amountStr));

                // Note
                t.setNote(row.getOrDefault("note", ""));

                // Dates (transaction_date, create_at, update_at)
                t.setTransactionDate(parseDateTime(row.get("transaction_date")));
                t.setCreate_at(parseDateTime(row.get("create_at")));
                t.setUpdate_at(parseDateTime(row.get("update_at")));
                t.setAccount(acc);

                // ✅ Category (UUID)
                String catIdStr = row.getOrDefault("category_id", "");
                if (catIdStr.isEmpty())
                    throw new IllegalArgumentException("Thiếu category_id");
                UUID catId = UUID.fromString(catIdStr);
                Category c = em.find(Category.class, catId);
                if (c == null)
                    throw new IllegalArgumentException("Category ID không tồn tại: " + catIdStr);
                t.setCategory(c);

                result.add(t);

            } catch (Exception e) {
                System.out.println("❌ Lỗi khi đọc dòng: " + raw);
                e.printStackTrace();
                throw new IllegalArgumentException("Lỗi khi đọc dòng dữ liệu: " + e.getMessage());
            }
        }

        em.close();
        return result;
    }

    private static java.time.LocalDateTime parseDateTime(String text) {
        if (text == null || text.isBlank()) return null;
        text = text.trim().replace(" ", "T");
        try {
            return java.time.LocalDateTime.parse(text);
        } catch (Exception e) {
            try {
                return java.time.LocalDate.parse(text).atStartOfDay();
            } catch (Exception ex) {
                return null;
            }
        }
    }

    public void saveTransactions(List<Transaction> transactions) {
        if (transactions != null && !transactions.isEmpty()) {
            dao.saveTransactions(transactions);
        }
    }

    public byte[] exportByAccount(UUID accountId, String start, String end, String format) throws Exception {
        LocalDate startDate = (start == null || start.isBlank()) ? LocalDate.MIN : LocalDate.parse(start);
        LocalDate endDate = (end == null || end.isBlank()) ? LocalDate.now() : LocalDate.parse(end);

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

        try (OutputStream os = resp.getOutputStream()) {
            os.write(data);
            os.flush();
        }
    }
}
