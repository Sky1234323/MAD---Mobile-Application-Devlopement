package com.example.practical3;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class DynamicActivity extends AppCompatActivity {

    private final List<String> items = new ArrayList<>();
    private CardAdapter adapter;
    private GridLayoutManager layoutManager;
    private TextInputEditText etLabel, etCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic);

        etLabel = findViewById(R.id.etLabel);
        etCount = findViewById(R.id.etCount);
        MaterialButtonToggleGroup toggleGroup = findViewById(R.id.toggleGroup);
        MaterialButton btnGenerate = findViewById(R.id.btnGenerate);
        RecyclerView rv = findViewById(R.id.rv);

        layoutManager = new GridLayoutManager(this, 2);
        rv.setLayoutManager(layoutManager);
        adapter = new CardAdapter(items);
        rv.setAdapter(adapter);

        // The number of columns changes the layout instantly
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            int columns = 2;
            if (checkedId == R.id.btnCol1) columns = 1;
            if (checkedId == R.id.btnCol3) columns = 3;
            layoutManager.setSpanCount(columns);
            adapter.notifyDataSetChanged();
        });

        btnGenerate.setOnClickListener(v -> generate());
        generate();
    }

    private void generate() {
        String label = etLabel.getText() == null ? "" : etLabel.getText().toString().trim();
        if (label.isEmpty()) {
            label = "Item";
        }

        String countText = etCount.getText() == null ? "" : etCount.getText().toString().trim();
        int count;
        try {
            count = Integer.parseInt(countText);
        } catch (NumberFormatException e) {
            count = 0;
        }
        if (count < 1 || count > 30) {
            etCount.setError("Enter a number from 1 to 30");
            return;
        }

        items.clear();
        for (int i = 1; i <= count; i++) {
            items.add(label + " " + i);
        }
        adapter.notifyDataSetChanged();
    }

    static class CardAdapter extends RecyclerView.Adapter<CardAdapter.VH> {

        private static final int[] COLORS = {
                0xFF4F46E5, 0xFF06B6D4, 0xFF10B981,
                0xFFF59E0B, 0xFFEF4444, 0xFF8B5CF6
        };

        private final List<String> data;

        CardAdapter(List<String> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_card, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            h.tv.setText(data.get(position));
            h.card.setCardBackgroundColor(COLORS[position % COLORS.length]);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            MaterialCardView card;
            TextView tv;

            VH(@NonNull View itemView) {
                super(itemView);
                card = itemView.findViewById(R.id.card);
                tv = itemView.findViewById(R.id.tvCard);
            }
        }
    }
}