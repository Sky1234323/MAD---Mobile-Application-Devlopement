package com.example.practical6;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

public class ToastActivity extends AppCompatActivity {

    private TextInputEditText etToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toast);

        etToast = findViewById(R.id.etToast);
        MaterialButton btnShort = findViewById(R.id.btnShort);
        MaterialButton btnLong = findViewById(R.id.btnLong);
        MaterialButton btnCustom = findViewById(R.id.btnCustom);
        MaterialButton btnSnack = findViewById(R.id.btnSnack);

        btnShort.setOnClickListener(v ->
                Toast.makeText(this, message(), Toast.LENGTH_SHORT).show());

        btnLong.setOnClickListener(v ->
                Toast.makeText(this, message(), Toast.LENGTH_LONG).show());

        btnCustom.setOnClickListener(v -> showCustomToast(message()));

        btnSnack.setOnClickListener(v ->
                Snackbar.make(findViewById(R.id.root), message(), Snackbar.LENGTH_LONG)
                        .setAction("UNDO", view ->
                                Toast.makeText(this, "Undo clicked", Toast.LENGTH_SHORT).show())
                        .show());
    }

    private String message() {
        String text = etToast.getText() == null ? "" : etToast.getText().toString().trim();
        return text.isEmpty() ? "Hello!" : text;
    }

    // Toast with our own layout (rounded dark card with a green tick)
    @SuppressWarnings("deprecation")
    private void showCustomToast(String text) {
        View layout = getLayoutInflater().inflate(R.layout.toast_custom, null);
        ((TextView) layout.findViewById(R.id.toastText)).setText(text);

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 220);
        toast.show();
    }
}
