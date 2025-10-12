package com.expensemanager.service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class SupabaseUserService {
    private final String supabaseUrl = System.getenv("SUPABASE_URL");
    private final String supabaseKey = System.getenv("SUPABASE_API_KEY");

    public boolean saveUser(String username, String email, String fullName, String provider) {
        try {
            String apiUrl = supabaseUrl + "/rest/v1/users";
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("apikey", supabaseKey);
            conn.setRequestProperty("Authorization", "Bearer " + supabaseKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String json = String.format(
                "{\"username\":\"%s\",\"email\":\"%s\",\"full_name\":\"%s\",\"provider\":\"%s\"}",
                username, email, fullName, provider
            );
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }
            int code = conn.getResponseCode();
            return code >= 200 && code < 300;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}