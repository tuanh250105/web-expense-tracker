package com.expensemanager.util;

import com.expensemanager.model.Transaction;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

public class CSVUtil {

    private static final String[] HEADERS = {
            "id", "account_id", "type", "category_id", "amount", "note",
            "transaction_date", "create_at", "update_at"
    };

    /**
     * Đọc CSV dạng danh sách key-value (phục vụ import preview)
     */
    public static List<Map<String, String>> readRawCSV(InputStream inputStream) throws IOException {
        List<Map<String, String>> list = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.trim().isEmpty()) {
                throw new IllegalArgumentException("File CSV không có header!");
            }

            // ✅ Xóa BOM và chuẩn hóa header
            headerLine = headerLine.replace("\uFEFF", "").trim();
            String[] headers = headerLine.split(",", -1);
            for (int i = 0; i < headers.length; i++) {
                headers[i] = headers[i]
                        .trim()
                        .replace("\"", "")
                        .toLowerCase(Locale.ROOT);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] values = line.split(",", -1);
                Map<String, String> row = new HashMap<>();

                for (int i = 0; i < headers.length && i < values.length; i++) {
                    String key = headers[i];
                    String value = values[i].trim().replace("\"", "");
                    row.put(key, value);
                }
                list.add(row);
            }

            // 🩵 Debug nhẹ
            if (!list.isEmpty()) {
                System.out.println("✅ CSVUtil đọc thành công " + list.size() + " dòng");
                System.out.println("🔑 Header: " + String.join(", ", headers));
            }
        }

        return list;
    }

    /**
     * Ghi danh sách Transaction ra file CSV
     */
    public static void writeCSV(OutputStream outputStream, List<Transaction> list) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            writer.write(String.join(",", HEADERS));
            writer.newLine();

            for (Transaction t : list) {
                writer.write(String.join(",",
                        safeStr(t.getId()),
                        safeStr(t.getAccount() != null ? t.getAccount().getId() : null),
                        safeStr(t.getType()),
                        safeStr(t.getCategory() != null ? t.getCategory().getId() : null),
                        String.valueOf(t.getAmount()),
                        safeStr(t.getNote()).replace(",", " "),
                        safeStrDate(t.getTransactionDate()),
                        safeStrDate(t.getCreate_at()),
                        safeStrDate(t.getUpdate_at())
                ));
                writer.newLine();
            }
        }
    }

    private static String safeStr(Object o) {
        return (o == null) ? "" : o.toString();
    }

    private static String safeStrDate(LocalDateTime dt) {
        return (dt == null) ? "" : dt.toString();
    }
}
