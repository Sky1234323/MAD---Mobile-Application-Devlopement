package com.example.practical1;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class SecondActivity extends AppCompatActivity {

    private static final String TAG = "LIFECYCLE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        Log.d(TAG, "SecondActivity: onCreate");

        TextView tvGreeting = findViewById(R.id.tvGreeting);
        MaterialButton btnBack = findViewById(R.id.btnBack);

        String name = getIntent().getStringExtra("USER_NAME");
        tvGreeting.setText("Hello, " + (name == null ? "Guest" : name) + "!");

        btnBack.setOnClickListener(v -> finish());
    }

    @Override protected void onStart()   { super.onStart();   Log.d(TAG, "SecondActivity: onStart"); }
    @Override protected void onResume()  { super.onResume();  Log.d(TAG, "SecondActivity: onResume"); }
    @Override protected void onPause()   { super.onPause();   Log.d(TAG, "SecondActivity: onPause"); }
    @Override protected void onStop()    { super.onStop();    Log.d(TAG, "SecondActivity: onStop"); }
    @Override protected void onDestroy() { super.onDestroy(); Log.d(TAG, "SecondActivity: onDestroy"); }
}