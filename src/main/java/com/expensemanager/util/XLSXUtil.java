package com.expensemanager.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

public class XLSXUtil {

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd/MM/yyyy");

    public static byte[] writeTransactionsToXLSX(List<Transaction> txs) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Transactions");

            // Header
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Ngày", "Mô tả", "Số tiền", "Danh mục", "Loại"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Data
            int rowNum = 1;
            for (Transaction t : txs) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(DATE_FMT.format(t.getTransactionDate()));
                row.createCell(1).setCellValue(t.getNote());
                row.createCell(2).setCellValue(t.getAmount().doubleValue());  // POI tự format, có thể thêm style nếu cần
                row.createCell(3).setCellValue(t.getCategory().getName());
                row.createCell(4).setCellValue(t.getType());
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
        }
    }
}