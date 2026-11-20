package com.example.practical3;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Locale;

public class VoiceActivity extends AppCompatActivity {

    private TextToSpeech tts;
    private boolean ttsReady = false;

    private TextView tvResult;
    private TextInputEditText etSpeak;
    private SwitchCompat switchRead;

    // Receives the text recognised from the user's speech
    private final ActivityResultLauncher<Intent> speechLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<String> list = result.getData()
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (list != null && !list.isEmpty()) {
                        String text = list.get(0);
                        tvResult.setText(text);
                        if (switchRead.isChecked()) {
                            speak("You said: " + text);
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);

        tvResult = findViewById(R.id.tvResult);
        etSpeak = findViewById(R.id.etSpeak);
        switchRead = findViewById(R.id.switchRead);
        MaterialButton btnListen = findViewById(R.id.btnListen);
        MaterialButton btnSpeak = findViewById(R.id.btnSpeak);

        // Text-to-Speech engine
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int r = tts.setLanguage(Locale.US);
                ttsReady = r != TextToSpeech.LANG_MISSING_DATA
                        && r != TextToSpeech.LANG_NOT_SUPPORTED;
            }
        });

        btnListen.setOnClickListener(v -> startListening());

        btnSpeak.setOnClickListener(v -> {
            String text = etSpeak.getText() == null ? "" : etSpeak.getText().toString().trim();
            if (text.isEmpty()) {
                etSpeak.setError("Type something first");
                return;
            }
            speak(text);
        });
    }

    private void startListening() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now");
        try {
            speechLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Speech recognition is not available on this phone",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void speak(String text) {
        if (!ttsReady) {
            Toast.makeText(this, "Text-to-speech is not ready yet", Toast.LENGTH_SHORT).show();
            return;
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utterance");
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}