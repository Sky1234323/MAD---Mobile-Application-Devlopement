package com.example.practical4;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.cardNotes).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, NotesActivity.class)));

        findViewById(R.id.cardStorage).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, StorageActivity.class)));
    }
}