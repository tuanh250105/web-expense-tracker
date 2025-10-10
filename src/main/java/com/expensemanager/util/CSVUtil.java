package com.expensemanager.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class CSVUtil {

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd/MM/yyyy");  // Khớp JSP

    public static List<Transaction> readTransactions(InputStream input) throws IOException {
        List<Transaction> list = new ArrayList<>();
        try (CSVParser parser = new CSVParser(new InputStreamReader(input), CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            for (CSVRecord record : parser) {
                Transaction tx = new Transaction();
                try {
                    tx.setTransactionDate(new Timestamp(DATE_FMT.parse(record.get("Ngày")).getTime()));
                    String amountStr = record.get("Số tiền").replace(",", "").replace("đ", "").trim();
                    tx.setAmount(new BigDecimal(amountStr));
                    tx.setNote(record.get("Mô tả"));
                    // Không cần "type", "category" trong file, sẽ set sau
                } catch (ParseException | NumberFormatException e) {
                    continue;  // Skip invalid
                }
                list.add(tx);
            }
        }
        return list;
    }

    public static byte[] writeTransactionsToCSV(List<Transaction> txs) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (CSVPrinter printer = new CSVPrinter(new java.io.PrintWriter(baos), CSVFormat.DEFAULT.withHeader("Ngày", "Mô tả", "Số tiền", "Danh mục", "Loại"))) {
            for (Transaction t : txs) {
                String date = DATE_FMT.format(t.getTransactionDate());
                String amount = t.getAmount().toPlainString() + "đ";
                String category = t.getCategory().getName();
                printer.printRecord(date, t.getNote(), amount, category, t.getType());
            }
        }
        return baos.toByteArray();
    }
}