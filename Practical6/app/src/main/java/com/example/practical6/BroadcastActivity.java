package com.example.practical6;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BroadcastActivity extends AppCompatActivity {

    // Name of our own (custom) broadcast
    public static final String CUSTOM_ACTION = "com.example.practical6.CUSTOM_ACTION";

    private TextView tvBattery, tvLog;
    private TextInputEditText etMessage;
    private final StringBuilder log = new StringBuilder();

    // One receiver that handles several kinds of broadcasts
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            switch (action) {
                case Intent.ACTION_BATTERY_CHANGED:
                    int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    if (level >= 0 && scale > 0) {
                        tvBattery.setText("Battery level: " + (level * 100 / scale) + "%");
                    }
                    break;
                case Intent.ACTION_POWER_CONNECTED:
                    addLog("Charger connected");
                    break;
                case Intent.ACTION_POWER_DISCONNECTED:
                    addLog("Charger disconnected");
                    break;
                case Intent.ACTION_AIRPLANE_MODE_CHANGED:
                    boolean on = intent.getBooleanExtra("state", false);
                    addLog("Airplane mode turned " + (on ? "ON" : "OFF"));
                    break;
                case CUSTOM_ACTION:
                    addLog("Custom broadcast received: "
                            + intent.getStringExtra("message"));
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast);

        tvBattery = findViewById(R.id.tvBattery);
        tvLog = findViewById(R.id.tvLog);
        etMessage = findViewById(R.id.etMessage);
        MaterialButton btnSend = findViewById(R.id.btnSend);
        MaterialButton btnClear = findViewById(R.id.btnClear);

        // Send our own broadcast; only this app receives it (setPackage)
        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText() == null
                    ? "" : etMessage.getText().toString().trim();
            Intent intent = new Intent(CUSTOM_ACTION);
            intent.setPackage(getPackageName());
            intent.putExtra("message", text.isEmpty() ? "(empty)" : text);
            sendBroadcast(intent);
        });

        btnClear.setOnClickListener(v -> {
            log.setLength(0);
            tvLog.setText("No events yet");
        });
    }

    // Register while the screen is visible, unregister when it is hidden
    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        filter.addAction(CUSTOM_ACTION);
        ContextCompat.registerReceiver(this, receiver, filter,
                ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
    }

    private void addLog(String message) {
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        log.insert(0, time + "  " + message + "\n");
        tvLog.setText(log.toString().trim());
    }
}