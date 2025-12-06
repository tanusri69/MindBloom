package com.mindbloom.threads;

import com.mindbloom.service.HabitTrackerService;
import com.mindbloom.service.MoodTrackerService;
import com.mindbloom.service.ReminderManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


public class ReminderThread extends Thread {
    private final ReminderManager reminderManager;
    private volatile boolean running = true;
    private static final long CHECK_INTERVAL_MS = 60000; 
    private LocalDate lastReminderDate = null;

    
    public ReminderThread(HabitTrackerService habitService, MoodTrackerService moodService) {
        this.reminderManager = new ReminderManager(habitService, moodService, 10); 
        setDaemon(true);
        setName("MindBloom-ReminderThread");
    }

    @Override
    public void run() {
        while (running) {
            try {
                checkReminders();
                Thread.sleep(CHECK_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    
    private void checkReminders() {
        LocalDate today = LocalDate.now();

        
        if (lastReminderDate != null && !lastReminderDate.equals(today)) {
            
        }
        lastReminderDate = today;

        
        List<String> pendingHabits = reminderManager.getPendingHabitReminders();
        for (String habitName : pendingHabits) {
            displayReminder(reminderManager.formatHabitReminder(habitName), "habit:" + habitName);
        }

        
        String stressMessage = reminderManager.getStressMotivation();
        if (stressMessage != null) {
            displayReminder(stressMessage, "stress");
        }
    }

    
    private void displayReminder(String message, String reminderId) {
        LocalTime now = LocalTime.now();
        String fullMessage = message;
        
        
        try {
            Class<?> mindBloomAppClass = Class.forName("com.mindbloom.ui.MindBloomApp");
            java.lang.reflect.Method addReminderMethod = mindBloomAppClass.getMethod("addReminderNotification", String.class);
            addReminderMethod.invoke(null, fullMessage);
        } catch (Exception e) {
            
            System.out.println("\n🔔 [" + now + "] " + fullMessage);
        }
    }

    
    public void snoozeHabitReminder(String habitName) {
        reminderManager.snoozeReminder("habit:" + habitName);
    }

    
    public void dismissHabitReminder(String habitName) {
        reminderManager.dismissReminder("habit:" + habitName);
    }

    
    public void stopReminders() {
        running = false;
    }
}
