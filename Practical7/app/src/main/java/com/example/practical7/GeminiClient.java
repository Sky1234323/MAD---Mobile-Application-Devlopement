package com.example.practical7;

import android.os.SystemClock;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class GeminiClient {

    // Encoded API key from aistudio.google.com
    private static final String API_KEY = new String(
            android.util.Base64.decode("QVEuQWI4Uk42SlZUaS0wVnQyWVdnQkFET21wd3FVUGxzU1hCYlFmUGhDWk9GSk95VkQ3UUE=", android.util.Base64.DEFAULT),
            StandardCharsets.UTF_8);

    // Models are tried in this order. If one is busy or missing, the next one is used.
    private static final String[] MODELS = {
            "gemini-3.5-flash-lite",
            "gemini-3.8-flash",
            "gemini-3.6-flash"
    };

    private static final long MAX_MS = 30000;

    private static final String PROMPT =
            "Summarize the following note in one or two short sentences (under 40 words). "
                    + "Use plain text only, with no markdown symbols.\n\nNote:\n";

    static class ApiException extends Exception {
        final int code;

        ApiException(int code, String message) {
            super(message);
            this.code = code;
        }
    }

    // Returns a short summary of the text. Must be called on a background thread.
    public static String summarize(String text) throws Exception {
        long start = SystemClock.elapsedRealtime();
        String lastError = "The AI service is busy. Tap Summarize to try again.";

        for (String model : MODELS) {
            long remaining = MAX_MS - (SystemClock.elapsedRealtime() - start);
            if (remaining < 4000) {
                break;
            }
            try {
                try {
                    return callModel(model, true, text, remaining);
                } catch (ApiException e) {
                    if (e.code == 400) {
                        // Some models reject the thinking setting, so retry without it
                        return callModel(model, false, text, remaining);
                    }
                    throw e;
                }
            } catch (ApiException e) {
                lastError = "The AI service is busy (HTTP " + e.code + "). Tap Summarize to retry.";
            } catch (SocketTimeoutException e) {
                lastError = "The AI service was slow. Tap Summarize to retry.";
            }
        }
        throw new Exception(lastError);
    }

    private static String callModel(String model, boolean fastThinking,
                                    String text, long remainingMs) throws Exception {
        JSONObject part = new JSONObject().put("text", PROMPT + text);
        JSONObject content = new JSONObject().put("parts", new JSONArray().put(part));

        JSONObject config = new JSONObject()
                .put("maxOutputTokens", 200)
                .put("temperature", 0.3);
        if (fastThinking) {
            config.put("thinkingConfig", new JSONObject().put("thinkingLevel", "minimal"));
        }

        JSONObject body = new JSONObject()
                .put("contents", new JSONArray().put(content))
                .put("generationConfig", config);

        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent";

        HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("x-goog-api-key", API_KEY);
        conn.setConnectTimeout((int) Math.min(5000, remainingMs));
        conn.setReadTimeout((int) Math.min(15000, remainingMs));
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        InputStream stream = (code >= 200 && code < 300)
                ? conn.getInputStream() : conn.getErrorStream();
        StringBuilder sb = new StringBuilder();
        if (stream != null) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
        }
        conn.disconnect();

        if (code != 200) {
            String msg = sb.toString();
            throw new ApiException(code, "HTTP " + code + " - "
                    + msg.substring(0, Math.min(200, msg.length())));
        }

        JSONObject json = new JSONObject(sb.toString());
        JSONArray candidates = json.optJSONArray("candidates");
        if (candidates == null || candidates.length() == 0) {
            throw new Exception("Empty response from the AI service");
        }
        JSONObject resultContent = candidates.getJSONObject(0).optJSONObject("content");
        JSONArray parts = resultContent == null ? null : resultContent.optJSONArray("parts");
        if (parts == null) {
            throw new Exception("Empty response from the AI service");
        }

        StringBuilder out = new StringBuilder();
        for (int i = 0; i < parts.length(); i++) {
            JSONObject p = parts.getJSONObject(i);
            if (!p.optBoolean("thought", false)) {
                out.append(p.optString("text", ""));
            }
        }
        String result = out.toString().trim();
        if (result.isEmpty()) {
            throw new Exception("Empty response from the AI service");
        }
        return result;
    }
}