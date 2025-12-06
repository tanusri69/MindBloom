package com.mindbloom.model;

import java.io.Serializable;
import java.time.LocalDate;


public class Habit implements Serializable {
    private static final long serialVersionUID = 1L;

    private String habitName;
    private int streak;
    private LocalDate lastCompleted;

    
    public Habit(String habitName) {
        this.habitName = habitName;
        this.streak = 0;
        this.lastCompleted = null;
    }

    
    public String getName() {
        return habitName;
    }

    
    public String getHabitName() {
        return habitName;
    }

    
    public int getStreak() {
        return streak;
    }

    
    public LocalDate getLastCompleted() {
        return lastCompleted;
    }

    
    public void incrementStreak() {
        this.streak++;
        this.lastCompleted = LocalDate.now();
    }

    
    public void resetStreak() {
        this.streak = 0;
    }

    @Override
    public String toString() {
        return "Habit{" +
                "name='" + habitName + '\'' +
                ", streak=" + streak +
                ", lastCompleted=" + lastCompleted +
                '}';
    }
}
