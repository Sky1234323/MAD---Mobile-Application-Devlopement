package com.example.practical3;


import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.cardLayouts).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LayoutsActivity.class)));

        findViewById(R.id.cardDynamic).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, DynamicActivity.class)));

        findViewById(R.id.cardVoice).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, VoiceActivity.class)));
    }
}