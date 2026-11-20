package com.example.practical4;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class NotesActivity extends AppCompatActivity implements NoteAdapter.Listener {

    private DBHelper db;
    private NoteAdapter adapter;
    private final List<Note> notes = new ArrayList<>();

    private TextInputEditText etTitle, etContent;
    private MaterialButton btnSave;
    private TextView tvInfo;

    // -1 means "adding a new note", any other value means "editing that note"
    private long editingId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        db = new DBHelper(this);

        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        btnSave = findViewById(R.id.btnSave);
        tvInfo = findViewById(R.id.tvInfo);
        RecyclerView rv = findViewById(R.id.rv);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoteAdapter(notes, this);
        rv.setAdapter(adapter);

        btnSave.setOnClickListener(v -> saveNote());
        loadNotes();
    }

    private void saveNote() {
        String title = etTitle.getText() == null ? "" : etTitle.getText().toString().trim();
        String content = etContent.getText() == null ? "" : etContent.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            return;
        }

        if (editingId == -1) {
            db.insertNote(title, content);                 // CREATE
            Toast.makeText(this, "Note added", Toast.LENGTH_SHORT).show();
        } else {
            db.updateNote(editingId, title, content);      // UPDATE
            Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
        }
        resetForm();
        loadNotes();
    }

    private void loadNotes() {
        notes.clear();
        notes.addAll(db.getAllNotes());                    // READ
        adapter.notifyDataSetChanged();
        tvInfo.setText(notes.isEmpty()
                ? "No notes yet. Add your first note."
                : notes.size() + " note(s) saved in the database");
    }

    private void resetForm() {
        etTitle.setText("");
        etContent.setText("");
        editingId = -1;
        btnSave.setText("Add Note");
    }

    @Override
    public void onEdit(Note note) {
        etTitle.setText(note.title);
        etContent.setText(note.content);
        editingId = note.id;
        btnSave.setText("Update Note");
    }

    @Override
    public void onDelete(Note note) {
        db.deleteNote(note.id);                            // DELETE
        if (editingId == note.id) {
            resetForm();
        }
        Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show();
        loadNotes();
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}