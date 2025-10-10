package com.expensemanager.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class PDFUtil {

    public static byte[] generate(List<Transaction> txs) throws IOException {
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);

        PDPageContentStream cs = new PDPageContentStream(doc, page);

        // Tiêu đề
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
        cs.newLineAtOffset(50, 780);
        cs.showText("Transactions Report");
        cs.endText();

        // Dữ liệu
        float y = 760;
        cs.setFont(PDType1Font.HELVETICA, 10);

        for (Transaction t : txs) {
            y -= 14;
            if (y < 50) break; // tránh tràn trang

            cs.beginText();
            cs.newLineAtOffset(50, y);

            String date = (t.getTransactionDate() != null)
                    ? t.getTransactionDate().toLocalDateTime().toLocalDate().toString()
                    : "";
            String category = (t.getCategory() != null) ? t.getCategory() : "";
            String amount = (t.getAmount() != null) ? t.getAmount().toPlainString() : "";
            String note = (t.getNote() != null) ? t.getNote() : "";

            // Format chuẩn không có lỗi dấu ngoặc
            String text = String.format("%s | %s | %s | %s", date, category, amount, note);
            cs.showText(text);

            cs.endText();
        }

        cs.close();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        doc.save(out);
        doc.close();
        return out.toByteArray();
    }
}
