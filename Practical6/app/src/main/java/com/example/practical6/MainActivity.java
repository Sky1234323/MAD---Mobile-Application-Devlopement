package com.example.practical6;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.cardBroadcast).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, BroadcastActivity.class)));

        findViewById(R.id.cardService).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ServiceActivity.class)));

        findViewById(R.id.cardToast).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ToastActivity.class)));
    }
}