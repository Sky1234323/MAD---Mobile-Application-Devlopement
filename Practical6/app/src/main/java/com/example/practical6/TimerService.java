package com.example.practical6;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;

public class TimerService extends Service {

    public static final String ACTION_TICK = "com.example.practical6.TIMER_TICK";
    public static final String EXTRA_SECONDS = "seconds";
    public static final int TOTAL = 10;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private int remaining;
    private boolean running = false;

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            remaining--;
            sendTick();
            if (remaining <= 0) {
                // Work finished: notify the user and stop the service
                NotificationHelper.show(TimerService.this, 2,
                        "Timer finished",
                        "The background service completed its " + TOTAL + " second task.");
                stopSelf();
            } else {
                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!running) {
            running = true;
            remaining = TOTAL;
            sendTick();
            handler.postDelayed(tick, 1000);
        }
        return START_NOT_STICKY;
    }

    // Tell the screen how many seconds are left (a broadcast sent by the service)
    private void sendTick() {
        Intent intent = new Intent(ACTION_TICK);
        intent.setPackage(getPackageName());
        intent.putExtra(EXTRA_SECONDS, remaining);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(tick);
        running = false;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
