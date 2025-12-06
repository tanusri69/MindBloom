package com.mindbloom.service;

import com.mindbloom.model.Habit;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;


public class ReminderManager {
    private final HabitTrackerService habitService;
    private final MoodTrackerService moodService;
    private final Map<String, LocalTime> lastSnoozeTime = new HashMap<>();
    private final int snoozeMinutes;
    private final Random random = new Random();

    
    public ReminderManager(HabitTrackerService habitService, MoodTrackerService moodService, int snoozeMinutes) {
        this.habitService = habitService;
        this.moodService = moodService;
        this.snoozeMinutes = snoozeMinutes;
    }

    
    public List<String> getPendingHabitReminders() {
        List<String> pending = new ArrayList<>();
        for (Habit habit : habitService.getAll()) {
            String habitName = habit.getName();
            
            
            java.time.LocalDate last = habit.getLastCompleted();
            boolean notDoneToday = (last == null) || last.isBefore(java.time.LocalDate.now());
            if (notDoneToday && !isReminderSnoozed(habitName)) {
                pending.add(habitName);
            }
        }
        return pending;
    }

    
    public boolean isReminderSnoozed(String reminderId) {
        if (!lastSnoozeTime.containsKey(reminderId)) {
            return false;
        }
        LocalTime snoozedUntil = lastSnoozeTime.get(reminderId).plusMinutes(snoozeMinutes);
        return LocalTime.now().isBefore(snoozedUntil);
    }

    
    public void snoozeReminder(String reminderId) {
        lastSnoozeTime.put(reminderId, LocalTime.now());
    }

    
    public void dismissReminder(String reminderId) {
        lastSnoozeTime.remove(reminderId);
    }

    
    public String checkStressTrigger() {
        String latestMood = moodService.getLatestMood();
        if (latestMood == null) {
            return null;
        }

        String moodLower = latestMood.toLowerCase();
        if (moodLower.contains("anxious") || moodLower.contains("stress") || 
            moodLower.contains("angry") || moodLower.contains("sad") ||
            moodLower.contains("worried") || moodLower.contains("nervous")) {
            
            String[] stressResponses = {
                "💙 We detected some stress. Take a deep breath and remember: this is temporary.",
                "🧘 It looks like you're feeling overwhelmed. How about a quick 5-minute break?",
                "💬 Tough times don't last, but tough people do. You've got this!",
                "🌸 Your feelings are valid. Be gentle with yourself right now.",
                "✨ This moment will pass. One step at a time."
            };
            return stressResponses[random.nextInt(stressResponses.length)];
        }
        return null;
    }

    
    public String formatHabitReminder(String habitName) {
        String[] reminders = {
                "🔔 Time to do '" + habitName + "'! Keep your streak alive!",
                "⏰ Don't forget: '" + habitName + "' - make it count today!",
                "💪 Ready for '" + habitName + "'? You're on a roll!",
                "🎯 Let's go: it's time for '" + habitName + "'!"
        };
        return reminders[random.nextInt(reminders.length)];
    }

    
    public String getStressMotivation() {
        return checkStressTrigger();
    }

    
    public int getSnoozeMinutes() {
        return snoozeMinutes;
    }
}
