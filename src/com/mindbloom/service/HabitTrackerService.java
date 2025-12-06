package com.mindbloom.service;

import com.mindbloom.model.Habit;
import com.mindbloom.exceptions.HabitNotFoundException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class HabitTrackerService implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Habit> habits = new ArrayList<>();

    
    public void addHabit(String name) {
        if (name == null || name.trim().isEmpty()) {
            System.out.println("❌ Habit name cannot be empty.");
            return;
        }
        
        
        for (Habit h : habits) {
            if (h.getName().equalsIgnoreCase(name)) {
                System.out.println("⚠️ Habit '" + name + "' already exists.");
                return;
            }
        }
        
        habits.add(new Habit(name));
        System.out.println("✅ Habit added: " + name);
    }

    
    public void addHabit(List<Habit> loadedHabits) {
        if (loadedHabits == null) {
            return;
        }
        habits.clear();
        habits.addAll(loadedHabits);
        System.out.println("✅ Loaded " + loadedHabits.size() + " habit(s)");
    }

    
    public List<Habit> getAll() {
        return habits;
    }

    
    public void tickHabit(String name) throws HabitNotFoundException {
        if (name == null || name.trim().isEmpty()) {
            throw new HabitNotFoundException("Habit name cannot be empty");
        }
        
        for (Habit h : habits) {
            if (h.getName().equalsIgnoreCase(name)) {
                h.incrementStreak();
                return;
            }
        }
        throw new HabitNotFoundException("Habit not found: " + name);
    }

    
    public void removeHabit(String name) throws HabitNotFoundException {
        for (int i = 0; i < habits.size(); i++) {
            if (habits.get(i).getName().equalsIgnoreCase(name)) {
                habits.remove(i);
                System.out.println("✅ Habit '" + name + "' removed.");
                return;
            }
        }
        throw new HabitNotFoundException("Habit not found: " + name);
    }
}
