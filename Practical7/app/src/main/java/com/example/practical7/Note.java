package com.example.practical7;

public class Note {
    public final String id;
    public final String title;
    public final String content;
    public final String summary;
    public final boolean summarizing;

    public Note(String id, String title, String content, String summary, boolean summarizing) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.summary = summary;
        this.summarizing = summarizing;
    }
}