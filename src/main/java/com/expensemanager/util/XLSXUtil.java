package com.expensemanager.util;

import com.expensemanager.model.Transaction;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XLSXUtil {

    private static final String[] HEADERS = {
            "id", "account_id", "type", "category_id", "amount", "note", "transaction_date", "create_at", "update_at"
    };

    public static List<Transaction> readXLSX(InputStream inputStream) throws IOException {
        List<Transaction> list = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean isHeader = true;

            for (Row row : sheet) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                Transaction t = new Transaction();
                t.setType(getString(row, 2));
                try {
                    t.setAmount((int) getNumeric(row, 4));
                } catch (Exception e) {
                    t.setAmount(0);
                }
                t.setNote(getString(row, 5));
                t.setTransactionDate(parseSafe(getString(row, 6)));
                t.setCreate_at(parseSafe(getString(row, 7)));
                t.setUpdate_at(parseSafe(getString(row, 8)));

                list.add(t);
            }
        }
        return list;
    }

    public static List<Map<String, String>> readRawXLSX(InputStream inputStream) throws IOException {
        List<Map<String, String>> list = new ArrayList<>();
        DataFormatter dataFormatter = new DataFormatter();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getRow(0) == null) {
                return list;
            }

            Row headerRow = sheet.getRow(0);
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(dataFormatter.formatCellValue(cell).trim());
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, String> rowMap = new HashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    String cellValue = (cell == null) ? "" : dataFormatter.formatCellValue(cell);
                    rowMap.put(headers.get(j), cellValue);
                }
                list.add(rowMap);
            }
        }
        return list;
    }

    private static LocalDateTime parseSafe(String text) {
        try {
            return (text != null && !text.isBlank()) ? LocalDateTime.parse(text.trim()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static String getString(Row row, int idx) {
        Cell cell = row.getCell(idx);
        return cell != null ? cell.toString().trim() : "";
    }

    private static double getNumeric(Row row, int idx) {
        Cell cell = row.getCell(idx);
        if (cell == null) return 0;
        return cell.getNumericCellValue();
    }

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
                row.createCell(6).setCellValue(t.getTransactionDate() != null ? t.getTransactionDate().toString() : "");
                row.createCell(7).setCellValue(t.getCreate_at() != null ? t.getCreate_at().toString() : "");
                row.createCell(8).setCellValue(t.getUpdate_at() != null ? t.getUpdate_at().toString() : "");
            }

            for (int i = 0; i < HEADERS.length; i++) sheet.autoSizeColumn(i);
            workbook.write(outputStream);
        }
    }
}
