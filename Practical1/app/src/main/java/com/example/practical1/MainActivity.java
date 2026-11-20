package com.example.practical1;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "LIFECYCLE";
    private TextInputEditText etName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "MainActivity: onCreate");

        etName = findViewById(R.id.etName);
        MaterialButton btnOpen = findViewById(R.id.btnOpen);
        MaterialButton btnWeb = findViewById(R.id.btnWeb);
        MaterialButton btnDial = findViewById(R.id.btnDial);
        MaterialButton btnShare = findViewById(R.id.btnShare);

        // Explicit intent: we name the exact class to open
        btnOpen.setOnClickListener(v -> {
            String name = etName.getText() == null ? "" : etName.getText().toString().trim();
            if (name.isEmpty()) {
                etName.setError("Please enter your name");
                return;
            }
            Intent intent = new Intent(MainActivity.this, SecondActivity.class);
            intent.putExtra("USER_NAME", name);
            startActivity(intent);
        });

        // Implicit intent: Android picks the app that can handle it
        btnWeb.setOnClickListener(v ->
                safeStart(new Intent(Intent.ACTION_VIEW, Uri.parse("https://developer.android.com"))));

        btnDial.setOnClickListener(v ->
                safeStart(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:100"))));

        btnShare.setOnClickListener(v -> {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, "Hello from my first Android app!");
            safeStart(Intent.createChooser(share, "Share via"));
        });
    }

    private void safeStart(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No app found to handle this action", Toast.LENGTH_SHORT).show();
        }
    }

    @Override protected void onStart()   { super.onStart();   Log.d(TAG, "MainActivity: onStart"); }
    @Override protected void onResume()  { super.onResume();  Log.d(TAG, "MainActivity: onResume"); }
    @Override protected void onPause()   { super.onPause();   Log.d(TAG, "MainActivity: onPause"); }
    @Override protected void onStop()    { super.onStop();    Log.d(TAG, "MainActivity: onStop"); }
    @Override protected void onRestart() { super.onRestart(); Log.d(TAG, "MainActivity: onRestart"); }
    @Override protected void onDestroy() { super.onDestroy(); Log.d(TAG, "MainActivity: onDestroy"); }
}