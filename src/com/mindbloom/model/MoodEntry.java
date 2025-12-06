package com.mindbloom.model;

import java.io.Serializable;
import java.time.LocalDate;


public class MoodEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private final LocalDate date;
    private final String mood;
    private final String note;

    
    public MoodEntry(String mood, String note) {
        this(LocalDate.now(), mood, note);
    }

    
    public MoodEntry(LocalDate date, String mood, String note) {
        this.date = date != null ? date : LocalDate.now();
        this.mood = mood;
        this.note = note != null ? note : "";
    }

    
    public LocalDate getDate() {
        return date;
    }

    
    public String getMood() {
        return mood;
    }

    
    public String getNote() {
        return note;
    }

    @Override
    public String toString() {
        String noteDisplay = note.isEmpty() ? "(no note)" : note;
        return "📅 " + date + " | 🙂 Mood: " + mood + " | 📝 Note: " + noteDisplay;
    }
}
