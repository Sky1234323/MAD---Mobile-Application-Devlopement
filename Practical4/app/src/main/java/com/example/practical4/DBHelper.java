package com.example.practical4;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "practical4.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE = "notes";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "title TEXT NOT NULL, "
                + "content TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    // CREATE
    public long insertNote(String title, String content) {
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("content", content);
        return getWritableDatabase().insert(TABLE, null, values);
    }

    // READ
    public List<Note> getAllNotes() {
        List<Note> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(
                TABLE, null, null, null, null, null, "id DESC")) {
            while (c.moveToNext()) {
                list.add(new Note(
                        c.getLong(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("title")),
                        c.getString(c.getColumnIndexOrThrow("content"))));
            }
        }
        return list;
    }

    // UPDATE
    public int updateNote(long id, String title, String content) {
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("content", content);
        return getWritableDatabase().update(
                TABLE, values, "id = ?", new String[]{String.valueOf(id)});
    }

    // DELETE
    public int deleteNote(long id) {
        return getWritableDatabase().delete(
                TABLE, "id = ?", new String[]{String.valueOf(id)});
    }
}