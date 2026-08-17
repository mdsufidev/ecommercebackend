package com.ecommerce.sufi.services;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GmailPasswordResetService {

    private static final URI TOKEN_URI = URI.create("https://oauth2.googleapis.com/token");
    private static final URI SEND_URI = URI.create("https://gmail.googleapis.com/gmail/v1/users/me/messages/send");

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final String clientId;
    private final String clientSecret;
    private final String refreshToken;
    private final String sender;
    private final String publicBaseUrl;

    public GmailPasswordResetService(ObjectMapper objectMapper,
            @Value("${gmail.api.enabled:false}") boolean enabled,
            @Value("${gmail.api.client-id:}") String clientId,
            @Value("${gmail.api.client-secret:}") String clientSecret,
            @Value("${gmail.api.refresh-token:}") String refreshToken,
            @Value("${gmail.api.sender:}") String sender,
            @Value("${app.public-base-url:http://localhost:8080}") String publicBaseUrl) {
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.refreshToken = refreshToken;
        this.sender = sender;
        this.publicBaseUrl = publicBaseUrl.replaceAll("/+$", "");
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void sendResetLink(String recipient, String token) {
        requireConfiguration();
        try {
            String accessToken = fetchAccessToken();
            String link = publicBaseUrl + "/reset-password?token="
                    + URLEncoder.encode(token, StandardCharsets.UTF_8);
            String rawMessage = buildMessage(recipient, link);
            String requestBody = objectMapper.writeValueAsString(Map.of("raw", rawMessage));
            HttpRequest request = HttpRequest.newBuilder(SEND_URI)
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new IllegalStateException("Gmail API rejected the email (HTTP " + response.statusCode() + ")");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Password reset email was interrupted", exception);
        } catch (Exception exception) {
            if (exception instanceof IllegalStateException stateException) throw stateException;
            throw new IllegalStateException("Could not send password reset email", exception);
        }
    }

    private String fetchAccessToken() throws Exception {
        String form = "client_id=" + encode(clientId)
                + "&client_secret=" + encode(clientSecret)
                + "&refresh_token=" + encode(refreshToken)
                + "&grant_type=refresh_token";
        HttpRequest request = HttpRequest.newBuilder(TOKEN_URI)
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IllegalStateException("Gmail OAuth authentication failed (HTTP " + response.statusCode() + ")");
        }
        Map<String, Object> json = objectMapper.readValue(response.body(), new TypeReference<>() { });
        Object token = json.get("access_token");
        if (token == null || token.toString().isBlank()) throw new IllegalStateException("Gmail OAuth returned no access token");
        return token.toString();
    }

    private String buildMessage(String recipient, String link) {
        String safeLink = HtmlUtils.htmlEscape(link);
        String html = "<div style=\"font-family:Arial,sans-serif;max-width:560px;margin:auto;color:#18201c\">"
                + "<h2 style=\"color:#1f4d3d\">Reset your Mango &amp; Mint password</h2>"
                + "<p>We received a request to reset your password.</p>"
                + "<p><a href=\"" + safeLink + "\" style=\"display:inline-block;padding:12px 18px;background:#1f4d3d;color:white;text-decoration:none;border-radius:5px\">Reset password</a></p>"
                + "<p>This link expires in 30 minutes and can only be used once.</p>"
                + "<p>If you did not request this, you can safely ignore this email.</p></div>";
        String mime = "From: Mango & Mint <" + sender + ">\r\n"
                + "To: " + recipient + "\r\n"
                + "Subject: Reset your Mango & Mint password\r\n"
                + "MIME-Version: 1.0\r\n"
                + "Content-Type: text/html; charset=UTF-8\r\n\r\n" + html;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(mime.getBytes(StandardCharsets.UTF_8));
    }

    private void requireConfiguration() {
        if (!enabled) throw new IllegalStateException("Gmail API email is disabled");
        if (clientId.isBlank() || clientSecret.isBlank() || refreshToken.isBlank() || sender.isBlank()) {
            throw new IllegalStateException("Gmail API credentials are incomplete");
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
