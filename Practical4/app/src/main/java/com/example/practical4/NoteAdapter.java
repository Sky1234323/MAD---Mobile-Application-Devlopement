package com.example.practical4;

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
        h.tvContent.setText(n.content == null || n.content.isEmpty()
                ? "(no content)" : n.content);
        h.btnEdit.setOnClickListener(v -> listener.onEdit(n));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(n));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent;
        MaterialButton btnEdit, btnDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
