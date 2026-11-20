package com.akash.practical2_;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AiActivity extends AppCompatActivity {

    // Encoded API key from aistudio.google.com
    private static final String API_KEY = new String(
            android.util.Base64.decode("QVEuQWI4Uk42SlZUaS0wVnQyWVdnQkFET21wd3FVUGxzU1hCYlFmUGhDWk9GSk95VkQ3UUE=", android.util.Base64.DEFAULT),
            StandardCharsets.UTF_8);

    // If you get "model not found" or "busy", copy another model name from AI Studio
    private static final String MODEL = "gemini-3.8-flash";

    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/"
                    + MODEL + ":generateContent";

    // Asks the model for short, plain-text answers
    private static final String STYLE =
            "Answer in plain text only. Do not use markdown symbols such as "
                    + "asterisks, hashes or backticks. Keep the answer under 120 words."
                    + "\n\nQuestion: ";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private LinearLayout chatContainer;
    private ScrollView scroll;
    private TextInputEditText etQuestion;
    private MaterialButton btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai);

        View root = findViewById(R.id.root);
        chatContainer = findViewById(R.id.chatContainer);
        scroll = findViewById(R.id.scroll);
        etQuestion = findViewById(R.id.etQuestion);
        btnSend = findViewById(R.id.btnSend);

        // Keep the input row above the keyboard and navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(0, 0, 0, bars.bottom);
            return insets;
        });

        addBubble("Hi! Ask me any question and I will answer it.", false);
        btnSend.setOnClickListener(v -> ask());
    }

    private void ask() {
        String question = etQuestion.getText() == null
                ? "" : etQuestion.getText().toString().trim();
        if (question.isEmpty()) {
            etQuestion.setError("Type a question first");
            return;
        }
        etQuestion.setText("");
        addBubble(question, true);
        final TextView reply = addBubble("Thinking...", false);
        btnSend.setEnabled(false);

        // Network call on a background thread, with up to 3 attempts on timeout
        executor.execute(() -> {
            String answer = null;
            for (int attempt = 1; attempt <= 3 && answer == null; attempt++) {
                try {
                    answer = callGemini(question);
                } catch (SocketTimeoutException e) {
                    if (attempt == 3) {
                        answer = "The AI server is slow right now. Please tap Send again.";
                    }
                } catch (Exception e) {
                    answer = "Error: " + e.getMessage();
                }
            }
            final String finalAnswer = answer;
            mainHandler.post(() -> {
                reply.setText(finalAnswer);
                btnSend.setEnabled(true);
                scrollToBottom();
            });
        });
    }

    private String callGemini(String question) throws Exception {
        // Request body: {"contents":[{"parts":[{"text":"..."}]}]}
        JSONObject part = new JSONObject().put("text", STYLE + question);
        JSONObject content = new JSONObject().put("parts", new JSONArray().put(part));
        JSONObject body = new JSONObject().put("contents", new JSONArray().put(content));

        HttpURLConnection conn = (HttpURLConnection) new URL(ENDPOINT).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("x-goog-api-key", API_KEY);
        conn.setConnectTimeout(20000);
        conn.setReadTimeout(40000);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        InputStream stream = (code >= 200 && code < 300)
                ? conn.getInputStream() : conn.getErrorStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        conn.disconnect();

        if (code != 200) {
            String text = sb.toString();
            throw new Exception("HTTP " + code + " - "
                    + text.substring(0, Math.min(250, text.length())));
        }

        // Response: candidates[0].content.parts[0].text
        JSONObject json = new JSONObject(sb.toString());
        return json.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
                .trim();
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
