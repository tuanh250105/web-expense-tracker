package com.expensemanager.util;

import com.expensemanager.model.Transaction;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.OutputStream;
import java.util.List;

public class PDFUtil {

    // Ghi danh sách Transaction ra file PDF
    public static void writePDF(OutputStream outputStream, List<Transaction> list) throws Exception {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, outputStream);
        document.open();

        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font bodyFont = new Font(Font.FontFamily.HELVETICA, 10);

        Paragraph title = new Paragraph("Transaction Report", headerFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" ")); // dòng trống

        PdfPTable table = new PdfPTable(9);
        table.setWidthPercentage(100);

        String[] headers = {
                "ID", "Account", "Type", "Category", "Amount", "Note", "Transaction Date", "Created At", "Updated At"
        };

        // Header table
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        // Nội dung từng dòng Transaction
        for (Transaction t : list) {
            table.addCell(getValue(t.getId() != null ? t.getId().toString() : "", bodyFont));
            table.addCell(getValue(t.getAccount() != null ? t.getAccount().getId().toString() : "", bodyFont));
            table.addCell(getValue(t.getType(), bodyFont));
            table.addCell(getValue(t.getCategory() != null ? t.getCategory().getId().toString() : "", bodyFont));
            table.addCell(getValue(String.valueOf(t.getAmount()), bodyFont));
            table.addCell(getValue(t.getNote(), bodyFont));
            table.addCell(getValue(t.getTransactionDate() != null ? t.getTransactionDate().toString() : "", bodyFont));
            table.addCell(getValue(t.getCreate_at() != null ? t.getCreate_at().toString() : "", bodyFont));
            table.addCell(getValue(t.getUpdate_at() != null ? t.getUpdate_at().toString() : "", bodyFont));
        }

        document.add(table);
        document.close();
    }

    private static PdfPCell getValue(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }
}
