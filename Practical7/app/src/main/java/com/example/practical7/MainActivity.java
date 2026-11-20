package com.example.practical7;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements NoteAdapter.Listener {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<Note> notes = new ArrayList<>();

    private CollectionReference notesRef;
    private ListenerRegistration registration;
    private NoteAdapter adapter;

    private TextInputEditText etTitle, etContent;
    private MaterialButton btnSave;
    private TextView tvInfo;

    // null means "adding a new note", otherwise the id of the note being edited
    private String editingId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        btnSave = findViewById(R.id.btnSave);
        tvInfo = findViewById(R.id.tvInfo);
        RecyclerView rv = findViewById(R.id.rv);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoteAdapter(notes, this);
        rv.setAdapter(adapter);

        // Firestore collection called "notes"
        notesRef = FirebaseFirestore.getInstance().collection("notes");

        btnSave.setOnClickListener(v -> saveNote());
        listenForNotes();
    }

    // Real-time listener: the list updates by itself whenever the data changes in Firebase
    private void listenForNotes() {
        registration = notesRef
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        tvInfo.setText("Firebase error: " + error.getMessage());
                        return;
                    }
                    if (snapshot == null) {
                        return;
                    }
                    notes.clear();
                    for (DocumentSnapshot d : snapshot.getDocuments()) {
                        notes.add(new Note(
                                d.getId(),
                                d.getString("title"),
                                d.getString("content"),
                                d.getString("summary"),
                                Boolean.TRUE.equals(d.getBoolean("summarizing"))));
                    }
                    adapter.notifyDataSetChanged();
                    tvInfo.setText(notes.isEmpty()
                            ? "No notes yet. Add your first note."
                            : notes.size() + " note(s) stored in Firebase (live)");
                });
    }

    private void saveNote() {
        final String title = etTitle.getText() == null
                ? "" : etTitle.getText().toString().trim();
        final String content = etContent.getText() == null
                ? "" : etContent.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            return;
        }
        if (content.isEmpty()) {
            etContent.setError("Write something to summarize");
            return;
        }

        if (editingId == null) {
            // CREATE: save to Firestore, then start the AI summary
            DocumentReference ref = notesRef.document();
            Map<String, Object> data = new HashMap<>();
            data.put("title", title);
            data.put("content", content);
            data.put("summary", "");
            data.put("summarizing", true);
            data.put("createdAt", System.currentTimeMillis());
            ref.set(data).addOnFailureListener(e ->
                    Toast.makeText(this, "Save failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show());
            summarize(ref.getId(), content);
            Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
        } else {
            // UPDATE: change the note, then create a fresh summary
            final String id = editingId;
            notesRef.document(id)
                    .update("title", title, "content", content, "summarizing", true)
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Update failed: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show());
            summarize(id, content);
            Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
        }
        resetForm();
    }

    // Asks the LLM for a summary in the background and stores it in the note
    private void summarize(String id, String content) {
        executor.execute(() -> {
            String summary;
            try {
                summary = GeminiClient.summarize(content);
            } catch (Exception e) {
                summary = e.getMessage();
            }
            notesRef.document(id).update("summary", summary, "summarizing", false);
        });
    }

    private void resetForm() {
        etTitle.setText("");
        etContent.setText("");
        editingId = null;
        btnSave.setText("Save Note");
    }

    @Override
    public void onSummarize(Note note) {
        notesRef.document(note.id).update("summarizing", true);
        summarize(note.id, note.content);
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
        notesRef.document(note.id).delete();               // DELETE
        if (note.id.equals(editingId)) {
            resetForm();
        }
        Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        if (registration != null) {
            registration.remove();
        }
        executor.shutdown();
        super.onDestroy();
    }
}