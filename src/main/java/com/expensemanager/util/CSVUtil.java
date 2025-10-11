package com.expensemanager.util;

import com.expensemanager.model.Transaction;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CSVUtil {

    private static final String[] HEADERS = {
            "id", "account_id", "type", "category_id", "amount", "note", "transaction_date", "create_at", "update_at"
    };

    public static List<Transaction> readCSV(InputStream inputStream) throws IOException {
        List<Transaction> list = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] parts = line.split(",", -1);
                if (parts.length < 9) continue;

                Transaction t = new Transaction();
                t.setType(parts[2]);

                try {
                    t.setAmount(Integer.parseInt(parts[4].trim().isEmpty() ? "0" : parts[4].trim()));
                } catch (Exception ex) {
                    t.setAmount(0);
                }

                t.setNote(parts[5]);
                t.setTransactionDate(safeParse(parts[6]));
                t.setCreate_at(safeParse(parts[7]));
                t.setUpdate_at(safeParse(parts[8]));
                list.add(t);
            }
        }
        return list;
    }

    private static LocalDateTime safeParse(String text) {
        try {
            return (text != null && !text.isBlank()) ? LocalDateTime.parse(text.trim()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static void writeCSV(OutputStream outputStream, List<Transaction> list) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            writer.write(String.join(",", HEADERS));
            writer.newLine();

            for (Transaction t : list) {
                writer.write(String.join(",",
                        String.valueOf(t.getId()),
                        t.getAccount() != null ? String.valueOf(t.getAccount().getId()) : "",
                        t.getType(),
                        t.getCategory() != null ? String.valueOf(t.getCategory().getId()) : "",
                        String.valueOf(t.getAmount()),
                        t.getNote() != null ? t.getNote().replace(",", " ") : "",
                        t.getTransactionDate() != null ? t.getTransactionDate().toString() : "",
                        t.getCreate_at() != null ? t.getCreate_at().toString() : "",
                        t.getUpdate_at() != null ? t.getUpdate_at().toString() : ""
                ));
                writer.newLine();
            }
        }
    }

    public static List<Map<String, String>> readRawCSV(InputStream inputStream) throws IOException {
        List<Map<String, String>> list = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String headerLine = reader.readLine();
            if (headerLine == null) return list;
            String[] headers = headerLine.split(",", -1);

            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",", -1);
                Map<String, String> row = new HashMap<>();
                for (int i = 0; i < headers.length && i < values.length; i++) {
                    row.put(headers[i].trim(), values[i].trim());
                }
                list.add(row);
            }
        }
        return list;
    }
}
