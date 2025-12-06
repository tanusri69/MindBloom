package com.mindbloom.service;

import com.mindbloom.model.Habit;
import com.mindbloom.model.MoodEntry;
import java.io.*;
import java.util.List;


public class StorageService {

    
    public static void saveToFile(String filename, List<MoodEntry> moods, List<Habit> habits) throws IOException {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }
        
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(moods);
            oos.writeObject(habits);
        }
    }

    
    @SuppressWarnings("unchecked")
    public static void loadFromFile(String filename, MoodTrackerService moodService, HabitTrackerService habitService)
            throws IOException, ClassNotFoundException {
        
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }
        
        File file = new File(filename);
        if (!file.exists()) {
            throw new FileNotFoundException("Storage file not found: " + filename);
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            List<MoodEntry> loadedMoods = (List<MoodEntry>) ois.readObject();
            List<Habit> loadedHabits = (List<Habit>) ois.readObject();

            moodService.setEntries(loadedMoods);
            habitService.addHabit(loadedHabits);
        }
    }
}
