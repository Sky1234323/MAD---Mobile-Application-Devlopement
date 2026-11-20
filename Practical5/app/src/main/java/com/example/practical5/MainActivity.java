package com.example.practical5;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    // Encoded API key from aistudio.google.com
    private static final String API_KEY = new String(
            android.util.Base64.decode("QVEuQWI4Uk42SlZUaS0wVnQyWVdnQkFET21wd3FVUGxzU1hCYlFmUGhDWk9GSk95VkQ3UUE=", android.util.Base64.DEFAULT),
            StandardCharsets.UTF_8);

    // Models are tried in this order. If one is busy or missing, the next one is used.
    // Replace these with exact names from the model list in AI Studio if needed.
    private static final String[] MODELS = {
            "gemini-3.5-flash-lite",
            "gemini-3.8-flash",
            "gemini-3.6-flash"
    };

    // Hard limit: the answer must finish within 30 seconds
    private static final long MAX_MS = 30000;

    private static final String STYLE =
            "You are a friendly assistant inside a mobile chat app. Answer in plain text "
                    + "only, with no markdown symbols such as asterisks, hashes or backticks. "
                    + "Keep answers short (under 100 words) unless asked for more.\n\n";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Conversation memory: each entry is {role, text}, role is "user" or "model"
    private final List<String[]> history = new ArrayList<>();

    private LinearLayout chatContainer;
    private ScrollView scroll;
    private TextInputEditText etMessage;
    private MaterialButton btnSend;
    private TextView tvStatus;

    static class ApiException extends Exception {
        final int code;

        ApiException(int code, String message) {
            super(message);
            this.code = code;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View root = findViewById(R.id.root);
        chatContainer = findViewById(R.id.chatContainer);
        scroll = findViewById(R.id.scroll);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        tvStatus = findViewById(R.id.tvStatus);
        MaterialButton btnNew = findViewById(R.id.btnNew);

        // Keep the input row above the keyboard and navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(0, 0, 0, bars.bottom);
            return insets;
        });

        addBubble("Hi! I am your AI chatbot. Ask me anything.", false);

        btnSend.setOnClickListener(v -> sendMessage());
        btnNew.setOnClickListener(v -> newChat());
    }

    private void newChat() {
        history.clear();
        chatContainer.removeAllViews();
        addBubble("New chat started. Ask me anything.", false);
        tvStatus.setText("Ready");
    }

    private void sendMessage() {
        String message = etMessage.getText() == null
                ? "" : etMessage.getText().toString().trim();
        if (message.isEmpty()) {
            etMessage.setError("Type a message first");
            return;
        }
        etMessage.setText("");
        addBubble(message, true);
        history.add(new String[]{"user", message});

        final TextView reply = addBubble("Typing...", false);
        btnSend.setEnabled(false);
        tvStatus.setText("Waiting for the reply...");

        final long start = SystemClock.elapsedRealtime();

        executor.execute(() -> {
            StringBuilder full = new StringBuilder();
            boolean timedOut = false;
            String error = null;
            String usedModel = "";

            // Try each model until one gives an answer, within the 30 second budget
            for (int m = 0; m < MODELS.length && full.length() == 0; m++) {
                long remaining = MAX_MS - (SystemClock.elapsedRealtime() - start);
                if (remaining < 4000) {
                    if (error == null) {
                        error = "The AI service is busy right now. Please try again in a moment.";
                    }
                    break;
                }
                try {
                    try {
                        timedOut = streamReply(MODELS[m], true, reply, full, start);
                    } catch (ApiException e) {
                        if (e.code == 400 && full.length() == 0) {
                            // Some models reject the thinking setting, so retry without it
                            timedOut = streamReply(MODELS[m], false, reply, full, start);
                        } else {
                            throw e;
                        }
                    }
                    usedModel = MODELS[m];
                } catch (SocketTimeoutException e) {
                    error = "The server is slow right now. Please try again.";
                } catch (ApiException e) {
                    if (e.code == 503 || e.code == 429) {
                        error = "The AI service is busy right now. Please try again in a moment.";
                    } else {
                        error = "Error: " + e.getMessage();
                    }
                } catch (Exception e) {
                    error = "Error: " + e.getMessage();
                }
            }

            final String finalText;
            if (full.length() > 0) {
                String text = full.toString().trim();
                history.add(new String[]{"model", text});
                finalText = timedOut ? text + "\n\n(Stopped after 30 seconds)" : text;
            } else {
                // Remove the unanswered question so the memory stays consistent
                history.remove(history.size() - 1);
                finalText = error != null ? error : "No answer received. Please try again.";
            }

            final double seconds = (SystemClock.elapsedRealtime() - start) / 1000.0;
            final String modelUsed = usedModel;
            mainHandler.post(() -> {
                reply.setText(finalText);
                tvStatus.setText(modelUsed.isEmpty()
                        ? String.format(Locale.US, "Response time: %.1f s", seconds)
                        : String.format(Locale.US, "Response time: %.1f s  |  Model: %s",
                        seconds, modelUsed));
                btnSend.setEnabled(true);
                scrollToBottom();
            });
        });
    }

    /**
     * Sends the conversation to one model and reads the answer as it streams in.
     * Returns true if the 30 second limit was reached before the answer finished.
     */
    private boolean streamReply(String model, boolean fastThinking, TextView reply,
                                StringBuilder full, long start) throws Exception {
        // Use only the latest turns so the request stays small and fast
        int from = Math.max(0, history.size() - 8);
        if (history.get(from)[0].equals("model")) {
            from++;
        }

        JSONArray contents = new JSONArray();
        for (int i = from; i < history.size(); i++) {
            String text = history.get(i)[1];
            if (i == from) {
                text = STYLE + text;
            }
            JSONObject part = new JSONObject().put("text", text);
            contents.put(new JSONObject()
                    .put("role", history.get(i)[0])
                    .put("parts", new JSONArray().put(part)));
        }

        JSONObject config = new JSONObject()
                .put("maxOutputTokens", 600)
                .put("temperature", 0.7);
        if (fastThinking) {
            config.put("thinkingConfig", new JSONObject().put("thinkingLevel", "minimal"));
        }

        JSONObject body = new JSONObject()
                .put("contents", contents)
                .put("generationConfig", config);

        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":streamGenerateContent?alt=sse";

        // Each attempt may only use the time that is left of the 30 seconds
        long remaining = MAX_MS - (SystemClock.elapsedRealtime() - start);
        HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("x-goog-api-key", API_KEY);
        conn.setConnectTimeout((int) Math.min(5000, remaining));
        conn.setReadTimeout((int) Math.min(10000, remaining));
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        if (code != 200) {
            InputStream err = conn.getErrorStream();
            StringBuilder sb = new StringBuilder();
            if (err != null) {
                BufferedReader r = new BufferedReader(new InputStreamReader(err));
                String l;
                while ((l = r.readLine()) != null) {
                    sb.append(l);
                }
                r.close();
            }
            conn.disconnect();
            String msg = sb.toString();
            throw new ApiException(code, "HTTP " + code + " - "
                    + msg.substring(0, Math.min(200, msg.length())));
        }

        boolean timedOut = false;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (SystemClock.elapsedRealtime() - start > MAX_MS) {
                    timedOut = true;
                    break;
                }
                if (!line.startsWith("data:")) {
                    continue;
                }
                String data = line.substring(5).trim();
                if (data.isEmpty() || data.equals("[DONE]")) {
                    continue;
                }

                JSONObject json = new JSONObject(data);
                JSONArray candidates = json.optJSONArray("candidates");
                if (candidates == null || candidates.length() == 0) {
                    continue;
                }
                JSONObject content = candidates.getJSONObject(0).optJSONObject("content");
                if (content == null) {
                    continue;
                }
                JSONArray parts = content.optJSONArray("parts");
                if (parts == null) {
                    continue;
                }
                for (int i = 0; i < parts.length(); i++) {
                    JSONObject p = parts.getJSONObject(i);
                    if (!p.optBoolean("thought", false)) {
                        full.append(p.optString("text", ""));
                    }
                }

                // Show the text received so far
                final String snapshot = full.toString();
                mainHandler.post(() -> {
                    reply.setText(snapshot);
                    scrollToBottom();
                });
            }
        } catch (SocketTimeoutException e) {
            if (full.length() == 0) {
                throw e;
            }
            timedOut = true;
        } finally {
            conn.disconnect();
        }
        return timedOut;
    }

    private TextView addBubble(String text, boolean isUser) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(15);
        tv.setTextColor(isUser ? Color.WHITE : Color.parseColor("#111827"));
        tv.setBackgroundResource(isUser ? R.drawable.bubble_user : R.drawable.bubble_ai);
        int pad = dp(14);
        tv.setPadding(pad, pad, pad, pad);
        tv.setMaxWidth(dp(290));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(6), 0, dp(6));
        lp.gravity = isUser ? Gravity.END : Gravity.START;
        tv.setLayoutParams(lp);

        chatContainer.addView(tv);
        scrollToBottom();
        return tv;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void scrollToBottom() {
        scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}