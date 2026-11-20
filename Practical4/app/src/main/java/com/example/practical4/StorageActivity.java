package com.example.practical4;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class StorageActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "user_prefs";
    private static final String FILE_NAME = "my_notes.txt";

    private TextView tvWelcome, tvFileContent;
    private TextInputEditText etName, etFileText;
    private SwitchCompat switchRemind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);

        tvWelcome = findViewById(R.id.tvWelcome);
        tvFileContent = findViewById(R.id.tvFileContent);
        TextView tvPath = findViewById(R.id.tvPath);
        etName = findViewById(R.id.etName);
        etFileText = findViewById(R.id.etFileText);
        switchRemind = findViewById(R.id.switchRemind);
        MaterialButton btnSavePrefs = findViewById(R.id.btnSavePrefs);
        MaterialButton btnSaveFile = findViewById(R.id.btnSaveFile);
        MaterialButton btnReadFile = findViewById(R.id.btnReadFile);
        MaterialButton btnDeleteFile = findViewById(R.id.btnDeleteFile);

        tvPath.setText("Stored at: " + getFilesDir() + "/" + FILE_NAME);

        loadPreferences();   // restores the saved values every time the screen opens

        btnSavePrefs.setOnClickListener(v -> savePreferences());
        btnSaveFile.setOnClickListener(v -> saveToFile());
        btnReadFile.setOnClickListener(v -> readFromFile());
        btnDeleteFile.setOnClickListener(v -> {
            boolean deleted = deleteFile(FILE_NAME);
            tvFileContent.setText(deleted ? "File deleted" : "No file to delete");
        });
    }

    // ---------- SharedPreferences ----------
    private void savePreferences() {
        String name = etName.getText() == null ? "" : etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Enter your name");
            return;
        }
        SharedPreferences.Editor editor =
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("name", name);
        editor.putBoolean("reminders", switchRemind.isChecked());
        editor.apply();

        tvWelcome.setText("Welcome back, " + name + "!");
        Toast.makeText(this, "Preferences saved", Toast.LENGTH_SHORT).show();
    }

    private void loadPreferences() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String name = prefs.getString("name", "");
        etName.setText(name);
        switchRemind.setChecked(prefs.getBoolean("reminders", false));
        tvWelcome.setText(name.isEmpty()
                ? "No preferences saved yet"
                : "Welcome back, " + name + "!");
    }

    // ---------- Internal file storage ----------
    private void saveToFile() {
        String text = etFileText.getText() == null ? "" : etFileText.getText().toString();
        if (text.trim().isEmpty()) {
            etFileText.setError("Type some text first");
            return;
        }
        try (FileOutputStream fos = openFileOutput(FILE_NAME, MODE_PRIVATE)) {
            fos.write(text.getBytes(StandardCharsets.UTF_8));
            Toast.makeText(this, "Saved to file", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void readFromFile() {
        try (FileInputStream fis = openFileInput(FILE_NAME);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(fis, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            tvFileContent.setText(sb.toString().trim());
        } catch (FileNotFoundException e) {
            tvFileContent.setText("No file saved yet");
        } catch (IOException e) {
            tvFileContent.setText("Error: " + e.getMessage());
        }
    }
}