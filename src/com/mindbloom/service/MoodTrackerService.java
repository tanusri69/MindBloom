package com.mindbloom.service;

import com.mindbloom.exceptions.InvalidMoodException;
import com.mindbloom.model.MoodEntry;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class MoodTrackerService implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<MoodEntry> moodEntries = new ArrayList<>();

    
    public void addEntry(LocalDate date, String mood, String note) throws InvalidMoodException {
        if (mood == null || mood.trim().isEmpty()) {
            throw new InvalidMoodException("Mood cannot be empty");
        }

        String normalized = normalizeMood(mood);
        if (normalized == null || normalized.trim().isEmpty()) {
            throw new InvalidMoodException("Mood could not be recognized. Try a simple word like 'happy' or 'calm'.");
        }
        if (normalized.matches("^\\d+$")) {
            throw new InvalidMoodException("Numeric moods are not allowed. Please enter a descriptive mood word.");
        }

        moodEntries.add(new MoodEntry(date, normalized, note != null ? note : ""));
    }

    
    public void addMood(String mood, String note) {
        try {
            addEntry(LocalDate.now(), mood, note);
        } catch (InvalidMoodException e) {
            System.out.println("❌ Error adding mood: " + e.getMessage());
        }
    }

    
    public List<MoodEntry> getAll() {
        return moodEntries;
    }

    
    public String getLatestMood() {
        if (moodEntries == null || moodEntries.isEmpty()) {
            return null;
        }
        MoodEntry last = moodEntries.get(moodEntries.size() - 1);
        return last != null ? last.getMood() : null;
    }

    
    public void setEntries(List<MoodEntry> newEntries) {
        moodEntries.clear();
        if (newEntries != null) {
            moodEntries.addAll(newEntries);
        }
    }

    
    private String normalizeMood(String raw) {
        if (raw == null) return "";
        String s = raw.trim().toLowerCase();
        
        s = s.replaceAll("[\\p{Punct}]", "");
        if (s.isEmpty()) return "";

        
        switch (s) {
            case "happy": case "joyful": case "elated": case "glad": case "cheerful":
                return "happy";
            case "sad": case "down": case "blue": case "unhappy":
                return "sad";
            case "anxious": case "nervous": case "stressed": case "worried":
                return "anxious";
            case "calm": case "peaceful": case "relaxed":
                return "calm";
            case "tired": case "sleepy": case "exhausted": case "fatigued":
                return "tired";
            case "angry": case "mad": case "irritated": case "frustrated":
                return "angry";
            default:
                
                if (s.contains("happy")) return "happy";
                if (s.contains("sad") || s.contains("down") || s.contains("blue")) return "sad";
                if (s.contains("anxi") || s.contains("nerv") || s.contains("stress") || s.contains("worr")) return "anxious";
                if (s.contains("calm") || s.contains("relax")) return "calm";
                if (s.contains("tire") || s.contains("sleep")) return "tired";
                if (s.contains("angr") || s.contains("mad") || s.contains("frustr")) return "angry";
                
                return s;
        }
    }

    
    public void saveData(String fileName) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(moodEntries);
        }
    }

    
    @SuppressWarnings("unchecked")
    public void loadData(String fileName) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            moodEntries = (ArrayList<MoodEntry>) ois.readObject();
        }
    }
}
