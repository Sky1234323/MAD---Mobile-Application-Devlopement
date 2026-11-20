package com.example.practical7;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.VH> {

    public interface Listener {
        void onSummarize(Note note);
        void onEdit(Note note);
        void onDelete(Note note);
    }

    private final List<Note> notes;
    private final Listener listener;

    public NoteAdapter(List<Note> notes, Listener listener) {
        this.notes = notes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Note n = notes.get(position);
        h.tvTitle.setText(n.title);
        h.tvContent.setText(n.content);

        if (n.summarizing) {
            h.tvSummary.setText("Summarizing with AI...");
        } else if (n.summary == null || n.summary.isEmpty()) {
            h.tvSummary.setText("No summary yet");
        } else {
            h.tvSummary.setText(n.summary);
        }

        h.btnSummarize.setEnabled(!n.summarizing);
        h.btnSummarize.setOnClickListener(v -> listener.onSummarize(n));
        h.btnEdit.setOnClickListener(v -> listener.onEdit(n));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(n));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSummary, tvContent;
        MaterialButton btnSummarize, btnEdit, btnDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSummary = itemView.findViewById(R.id.tvSummary);
            tvContent = itemView.findViewById(R.id.tvContent);
            btnSummarize = itemView.findViewById(R.id.btnSummarize);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
