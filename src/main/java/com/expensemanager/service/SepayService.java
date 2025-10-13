package com.expensemanager.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * SePay API Integration Service
 * Đồng bộ giao dịch ngân hàng từ SePay API
 */
public class SepayService {
    
    private static final String API_TOKEN = "I7RQMT4YB9TNZQI0FJLA1XPHVSNZH6TDBLRFE23AYFXQSLM1D4KYTBJDXPV5YK8L";
    private static final String API_URL = "https://my.sepay.vn/userapi/transactions/list";
    
    private final HttpClient httpClient;
    private final Gson gson;
    
    public SepayService() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }
    
    /**
     * Lấy giao dịch từ SePay API trong khoảng thời gian
     */
    public List<SepayTransaction> fetchTransactions(int days) throws IOException, InterruptedException {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String startDateStr = startDate.format(formatter);
        String endDateStr = endDate.format(formatter);
        
        // Build URL với parameters
        String urlWithParams = String.format(
            "%s?transaction_date_min=%s&transaction_date_max=%s&limit=100",
            API_URL, 
            startDateStr.replace(" ", "%20"),
            endDateStr.replace(" ", "%20")
        );
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(urlWithParams))
            .header("Authorization", "Bearer " + API_TOKEN)
            .header("Content-Type", "application/json")
            .GET()
            .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new IOException("SePay API Error: " + response.statusCode() + " - " + response.body());
        }
        
        return parseTransactions(response.body());
    }
    
    /**
     * Parse JSON response từ SePay API
     */
    private List<SepayTransaction> parseTransactions(String jsonResponse) {
        List<SepayTransaction> transactions = new ArrayList<>();
        
        try {
            JsonObject responseObj = JsonParser.parseString(jsonResponse).getAsJsonObject();
            
            if (responseObj.has("transactions")) {
                JsonArray transactionArray = responseObj.getAsJsonArray("transactions");
                
                for (int i = 0; i < transactionArray.size(); i++) {
                    JsonObject txObj = transactionArray.get(i).getAsJsonObject();
                    
                    double amountIn = txObj.has("amount_in") ? txObj.get("amount_in").getAsDouble() : 0.0;
                    double amountOut = txObj.has("amount_out") ? txObj.get("amount_out").getAsDouble() : 0.0;
                    double amount = amountIn - amountOut; // Positive = thu nhập, Negative = chi tiêu
                    
                    SepayTransaction transaction = new SepayTransaction();
                    transaction.setId(txObj.has("id") ? txObj.get("id").getAsString() : "");
                    transaction.setReferenceNumber(txObj.has("reference_number") ? txObj.get("reference_number").getAsString() : "");
                    transaction.setAmount(amount);
                    transaction.setContent(txObj.has("transaction_content") ? txObj.get("transaction_content").getAsString() : "");
                    transaction.setTransactionDate(txObj.has("transaction_date") ? txObj.get("transaction_date").getAsString() : "");
                    transaction.setAccountNumber(txObj.has("account_number") ? txObj.get("account_number").getAsString() : "");
                    transaction.setBankName(txObj.has("bank_brand_name") ? txObj.get("bank_brand_name").getAsString() : "");
                    transaction.setType(amount > 0 ? "INCOME" : "EXPENSE");
                    transaction.setAmountIn(amountIn);
                    transaction.setAmountOut(amountOut);
                    
                    transactions.add(transaction);
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing SePay response: " + e.getMessage());
            e.printStackTrace();
        }
        
        return transactions;
    }
    
    /**
     * Model class cho SePay Transaction
     */
    public static class SepayTransaction {
        private String id;
        private String referenceNumber;
        private double amount;
        private String content;
        private String transactionDate;
        private String accountNumber;
        private String bankName;
        private String type;
        private double amountIn;
        private double amountOut;
        private boolean processed = false;
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getReferenceNumber() { return referenceNumber; }
        public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
        
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getTransactionDate() { return transactionDate; }
        public void setTransactionDate(String transactionDate) { this.transactionDate = transactionDate; }
        
        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
        
        public String getBankName() { return bankName; }
        public void setBankName(String bankName) { this.bankName = bankName; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public double getAmountIn() { return amountIn; }
        public void setAmountIn(double amountIn) { this.amountIn = amountIn; }
        
        public double getAmountOut() { return amountOut; }
        public void setAmountOut(double amountOut) { this.amountOut = amountOut; }
        
        public boolean isProcessed() { return processed; }
        public void setProcessed(boolean processed) { this.processed = processed; }
    }
}