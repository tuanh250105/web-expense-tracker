package com.expensemanager.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collections;

import com.expensemanager.model.User;
import com.expensemanager.service.AuthService;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns={"/auth/google/start","/auth/google/callback"})
public class GoogleOAuthServlet extends HttpServlet {
    private final AuthService service = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        switch (req.getServletPath()) {
            case "/auth/google/start" -> start(req, resp);
            case "/auth/google/callback" -> callback(req, resp);
            default -> resp.sendError(404);
        }
    }

    private void start(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String appBase = System.getenv("APP_BASE_URL");
        if (appBase == null || appBase.isBlank()) {
            String scheme = req.getScheme();
            String host = req.getServerName();
            int port = req.getServerPort();
            String portPart = (port == 80 || port == 443) ? "" : ":" + port;
            String context = req.getContextPath();
            appBase = scheme + "://" + host + portPart + context;
        }
        String redirect = appBase + "/auth/google/callback";
        String url = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?response_type=code"
                + "&client_id=" + URLEncoder.encode(System.getenv("GOOGLE_CLIENT_ID"), StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirect, StandardCharsets.UTF_8)
                + "&scope=" + URLEncoder.encode("openid email profile", StandardCharsets.UTF_8);
        resp.sendRedirect(url);
    }

    private void callback(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        String code = req.getParameter("code");
        if (code == null) {
            resp.sendRedirect(req.getContextPath()+"/views/auth/login.jsp?error=google");
            return;
        }

        String appBase = System.getenv("APP_BASE_URL");
        if (appBase == null || appBase.isBlank()) {
            String scheme = req.getScheme();
            String host = req.getServerName();
            int port = req.getServerPort();
            String portPart = (port == 80 || port == 443) ? "" : ":" + port;
            String context = req.getContextPath();
            appBase = scheme + "://" + host + portPart + context;
        }
        String redirect = appBase + "/auth/google/callback";
        var transport = new NetHttpTransport();
        var jsonFactory = GsonFactory.getDefaultInstance();

        System.out.println("🔍 [Google OAuth Callback]");
        System.out.println("code=" + code);
        System.out.println("redirect=" + redirect);
        System.out.println("clientId=" + System.getenv("GOOGLE_CLIENT_ID"));
        System.out.println("clientSecret=" + System.getenv("GOOGLE_CLIENT_SECRET").substring(0,5) + "...");

        GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                transport, jsonFactory, "https://oauth2.googleapis.com/token",
                System.getenv("GOOGLE_CLIENT_ID"),
                System.getenv("GOOGLE_CLIENT_SECRET"),
                code, redirect).execute();

        GoogleIdToken idToken = GoogleIdToken.parse(jsonFactory, tokenResponse.getIdToken());
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier
                .Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(System.getenv("GOOGLE_CLIENT_ID"))).build();

        boolean ok;
        try {
            ok = verifier.verify(idToken);
        } catch (GeneralSecurityException e) {
            ok = false;
        }
        if (!ok) {
            resp.sendRedirect(req.getContextPath()+"/views/auth/login.jsp?error=verify");
            return;
        }

        var payload = idToken.getPayload();
        User user = service.loginOrCreateGoogle(payload.getEmail(), (String) payload.get("name"));
        req.getSession(true).setAttribute("user", user);
        resp.sendRedirect(req.getContextPath()+"/layout/layout.jsp");
    }
}