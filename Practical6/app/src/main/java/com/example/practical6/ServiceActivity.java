package com.example.practical6;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ServiceActivity extends AppCompatActivity {

    private static final int NOTIF_ID = 1;

    private TextInputEditText etNotif;
    private TextView tvStatus;
    private ProgressBar progress;

    // Action to run after the user allows notifications
    private Runnable pendingAction;

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted && pendingAction != null) {
                    pendingAction.run();
                } else if (!granted) {
                    Toast.makeText(this, "Notification permission denied",
                            Toast.LENGTH_LONG).show();
                }
                pendingAction = null;
            });

    // Receives the "seconds left" broadcasts sent by TimerService
    private final BroadcastReceiver tickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int remaining = intent.getIntExtra(TimerService.EXTRA_SECONDS, 0);
            progress.setProgress(TimerService.TOTAL - remaining);
            if (remaining > 0) {
                tvStatus.setText("Service running... " + remaining + " s left");
            } else {
                tvStatus.setText("Service finished. Notification sent.");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);

        etNotif = findViewById(R.id.etNotif);
        tvStatus = findViewById(R.id.tvStatus);
        progress = findViewById(R.id.progress);
        MaterialButton btnShow = findViewById(R.id.btnShow);
        MaterialButton btnCancel = findViewById(R.id.btnCancel);
        MaterialButton btnStart = findViewById(R.id.btnStart);
        MaterialButton btnStop = findViewById(R.id.btnStop);

        NotificationHelper.createChannel(this);

        btnShow.setOnClickListener(v -> withPermission(() -> {
            String text = etNotif.getText() == null
                    ? "" : etNotif.getText().toString().trim();
            NotificationHelper.show(this, NOTIF_ID, "Practical 6",
                    text.isEmpty() ? "Hello!" : text);
        }));

        btnCancel.setOnClickListener(v -> NotificationHelper.cancel(this, NOTIF_ID));

        btnStart.setOnClickListener(v -> withPermission(() -> {
            progress.setProgress(0);
            startService(new Intent(this, TimerService.class));
        }));

        btnStop.setOnClickListener(v -> {
            stopService(new Intent(this, TimerService.class));
            tvStatus.setText("Service stopped");
            progress.setProgress(0);
        });
    }

    // Asks for the notification permission (Android 13 and above) before running an action
    private void withPermission(Runnable action) {
        if (NotificationHelper.hasPermission(this)) {
            action.run();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pendingAction = action;
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        ContextCompat.registerReceiver(this, tickReceiver,
                new IntentFilter(TimerService.ACTION_TICK),
                ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(tickReceiver);
    }
}