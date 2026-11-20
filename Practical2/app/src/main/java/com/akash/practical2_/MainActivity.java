package com.akash.practical2_;




import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    // DummyJSON returns: {"posts":[ {id, title, body, ...} ]}
    private static final String API_URL = "https://dummyjson.com/posts?limit=15";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final List<Post> posts = new ArrayList<>();

    private PostAdapter adapter;
    private ProgressBar progress;
    private TextView tvStatus;
    private String rawJson = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progress = findViewById(R.id.progress);
        tvStatus = findViewById(R.id.tvStatus);
        RecyclerView rv = findViewById(R.id.rv);
        MaterialButton btnFetch = findViewById(R.id.btnFetch);
        MaterialButton btnRaw = findViewById(R.id.btnRaw);
        MaterialButton btnAi = findViewById(R.id.btnAi);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PostAdapter(posts);
        rv.setAdapter(adapter);

        btnFetch.setOnClickListener(v -> fetchData());
        btnRaw.setOnClickListener(v -> showRawJson());
        btnAi.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AiActivity.class)));

        fetchData();
    }

    private void fetchData() {
        progress.setVisibility(View.VISIBLE);
        tvStatus.setText("Loading...");

        // Network calls must run on a background thread
        executor.execute(() -> {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                conn.disconnect();

                // The list is inside the "posts" key of the response object
                JSONObject root = new JSONObject(sb.toString());
                final String pretty = root.toString(2);
                JSONArray arr = root.getJSONArray("posts");

                final List<Post> result = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    result.add(new Post(
                            o.getInt("id"),
                            o.getString("title"),
                            o.getString("body")));
                }

                // UI updates must run on the main thread
                mainHandler.post(() -> {
                    rawJson = pretty;
                    posts.clear();
                    posts.addAll(result);
                    adapter.notifyDataSetChanged();
                    progress.setVisibility(View.GONE);
                    tvStatus.setText(result.size() + " posts loaded");
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    progress.setVisibility(View.GONE);
                    tvStatus.setText("Error: " + e.getMessage());
                });
            }
        });
    }

    private void showRawJson() {
        String text = rawJson.isEmpty()
                ? "No data yet. Tap Fetch Data first."
                : rawJson.substring(0, Math.min(1500, rawJson.length()));
        new MaterialAlertDialogBuilder(this)
                .setTitle("Raw JSON response")
                .setMessage(text)
                .setPositiveButton("Close", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}