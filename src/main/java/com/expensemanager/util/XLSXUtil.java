package com.expensemanager.util;

import com.expensemanager.model.Transaction;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class XLSXUtil {

    private static final String[] HEADERS = {
            "id", "account_id", "type", "category_id", "amount", "note",
            "transaction_date", "create_at", "update_at"
    };

    /** ======================= ĐỌC FILE EXCEL (Preview Import) ======================= **/
    public static List<Map<String, String>> readRawXLSX(InputStream inputStream) throws IOException {
        List<Map<String, String>> list = new ArrayList<>();
        DataFormatter fmt = new DataFormatter();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getRow(0) == null) return list;

            // Lấy header
            Row headerRow = sheet.getRow(0);
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                String header = fmt.formatCellValue(cell)
                        .replace("\uFEFF", "")
                        .trim()
                        .replace("\"", "")
                        .toLowerCase(Locale.ROOT);
                headers.add(header);
            }

            // Đọc từng dòng
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, String> rowMap = new HashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    String cellValue = (cell == null) ? "" : fmt.formatCellValue(cell).trim();
                    rowMap.put(headers.get(j), cellValue);
                }

                boolean allEmpty = rowMap.values().stream().allMatch(String::isEmpty);
                if (!allEmpty) list.add(rowMap);
            }

            System.out.println("✅ XLSXUtil đọc thành công " + list.size() + " dòng.");
            System.out.println("🔑 Header: " + String.join(", ", headers));
        }

        return list;
    }

    /** ======================= GHI FILE EXCEL (Export) ======================= **/
    public static void writeXLSX(OutputStream outputStream, List<Transaction> list) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Transactions");
            Row header = sheet.createRow(0);

            for (int i = 0; i < HEADERS.length; i++) {
                header.createCell(i).setCellValue(HEADERS[i]);
            }

            int rowIndex = 1;
            for (Transaction t : list) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(t.getId() != null ? t.getId().toString() : "");
                row.createCell(1).setCellValue(t.getAccount() != null ? t.getAccount().getId().toString() : "");
                row.createCell(2).setCellValue(t.getType());
                row.createCell(3).setCellValue(t.getCategory() != null ? t.getCategory().getId().toString() : "");
                row.createCell(4).setCellValue(t.getAmount());
                row.createCell(5).setCellValue(t.getNote());
                row.createCell(6).setCellValue(formatDateTime(t.getTransactionDate()));
                row.createCell(7).setCellValue(formatDateTime(t.getCreate_at()));
                row.createCell(8).setCellValue(formatDateTime(t.getUpdate_at()));
            }

            for (int i = 0; i < HEADERS.length; i++) sheet.autoSizeColumn(i);
            workbook.write(outputStream);
        }
    }

    /** ======================= ĐỌC THÀNH DANH SÁCH TRANSACTION ======================= **/
    public static List<Transaction> readXLSX(InputStream inputStream) throws IOException {
        List<Transaction> list = new ArrayList<>();
        DataFormatter fmt = new DataFormatter();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) return list;

            boolean isHeader = true;

            for (Row row : sheet) {
                if (isHeader) { isHeader = false; continue; }

                String rawType = text(fmt, row.getCell(2));
                String rawAmount = text(fmt, row.getCell(4));
                String note = text(fmt, row.getCell(5));
                String rawTxDate = text(fmt, row.getCell(6));
                String rawCreateAt = text(fmt, row.getCell(7));
                String rawUpdateAt = text(fmt, row.getCell(8));

                int amount = parseAmountToInt(rawAmount);
                String type = normalizeType(rawType, amount);

                if (type == null) {
                    throw new IllegalArgumentException("type không hợp lệ: '" + rawType + "'");
                }

                Transaction t = new Transaction();
                t.setType(type);
                t.setAmount(amount);
                t.setNote(note);
                t.setTransactionDate(parseSafeDateTime(rawTxDate));
                t.setCreate_at(parseSafeDateTime(rawCreateAt));
                t.setUpdate_at(parseSafeDateTime(rawUpdateAt));
                list.add(t);
            }
        }

        System.out.println("📘 XLSXUtil: Đã đọc " + list.size() + " transaction từ Excel.");
        return list;
    }

    /** ======================= HÀM HỖ TRỢ ======================= **/

    private static String text(DataFormatter fmt, Cell cell) {
        return (cell == null) ? "" : fmt.formatCellValue(cell).trim();
    }

    private static String normalizeType(String raw, int amount) {
        if (raw == null) raw = "";
        String s = raw.trim().toLowerCase(Locale.ROOT);

        if (s.isEmpty()) return amount < 0 ? "expense" : "income";

        if (s.matches("(?i)(thu|income|in|credit|deposit|nạp|+|\\+)")) return "income";
        if (s.matches("(?i)(chi|expense|out|debit|withdraw|rút|\\-|–)")) return "expense";

        if (s.contains("thu") || s.contains("income")) return "income";
        if (s.contains("chi") || s.contains("expense") || s.contains("spend") || s.contains("out")) return "expense";

        return amount < 0 ? "expense" : (amount > 0 ? "income" : null);
    }

    private static int parseAmountToInt(String raw) {
        if (raw == null) return 0;
        String s = raw.trim();

        boolean bracketNegative = s.startsWith("(") && s.endsWith(")");
        s = s.replaceAll("[^0-9,\\.\\-]", "").replace(",", "").replace(".", "");

        if (s.isEmpty() || s.equals("-")) return 0;
        try {
            int val = Integer.parseInt(s);
            if (bracketNegative) val = -Math.abs(val);
            return val;
        } catch (Exception e) {
            return 0;
        }
    }

    private static LocalDateTime parseSafeDateTime(String text) {
        if (text == null || text.isBlank()) return null;
        String s = text.trim();

        try {
            if (s.matches("\\d{4}-\\d{2}-\\d{2}")) return LocalDate.parse(s).atStartOfDay();
            if (s.matches("\\d{2}/\\d{2}/\\d{4}")) {
                String[] p = s.split("/");
                return LocalDate.of(Integer.parseInt(p[2]), Integer.parseInt(p[1]), Integer.parseInt(p[0])).atStartOfDay();
            }
            if (s.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}(:\\d{2})?")) s = s.replace(" ", "T");
            return LocalDateTime.parse(s);
        } catch (Exception e) {
            System.err.println("⚠️ Không parse được thời gian: '" + text + "'");
            return null;
        }
    }

    private static String formatDateTime(LocalDateTime dt) {
        return (dt != null) ? dt.toString().replace("T", " ") : "";
    }
}
