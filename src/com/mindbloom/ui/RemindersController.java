package com.mindbloom.ui;

import com.mindbloom.service.HabitTrackerService;
import com.mindbloom.service.ReminderManager;
import com.mindbloom.service.MoodTrackerService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import com.mindbloom.ui.AnimationUtil;


public class RemindersController {
    @FXML
    private Button pendingHabitsButton;
    @FXML
    private Button stressCheckButton;
    @FXML
    private Button snoozeSettingsButton;
    @FXML
    private Button refreshButton;
    @FXML
    private TextArea remindersArea;
    @FXML
    private Label statusLabel;

    private HabitTrackerService habitService;
    private MoodTrackerService moodService;
    private ReminderManager reminderManager;

    @FXML
    public void initialize() {
        pendingHabitsButton.setOnAction(event -> showPendingHabits());
        stressCheckButton.setOnAction(event -> checkStress());
        snoozeSettingsButton.setOnAction(event -> showSnoozeSettings());
        refreshButton.setOnAction(event -> refresh());

        
        AnimationUtil.fadeIn(remindersArea);
        AnimationUtil.pulse(refreshButton);
    }

    public void setServices(HabitTrackerService habitService, MoodTrackerService moodService) {
        this.habitService = habitService;
        this.moodService = moodService;
        this.reminderManager = new ReminderManager(habitService, moodService, 10);
        remindersArea.setText("Reminders system ready.");
    }

    private void showPendingHabits() {
        if (habitService == null) {
            statusLabel.setText("❌ Services not initialized.");
            return;
        }
        StringBuilder sb = new StringBuilder("📋 Pending Habits:\n\n");
        habitService.getAll().forEach(habit ->
                sb.append("• ").append(habit.getName())
                  .append(" (Streak: ").append(habit.getStreak()).append(")\n")
        );
        remindersArea.setText(sb.toString());
        statusLabel.setText("✅ Showing pending habits.");
    }

    private void checkStress() {
        if (reminderManager == null) {
            statusLabel.setText("❌ Services not initialized.");
            return;
        }
        String stressResult = reminderManager.checkStressTrigger();
        if (stressResult != null && !stressResult.isEmpty()) {
            remindersArea.setText("⚠️ STRESS DETECTED!\n\n" +
                    stressResult + "\n" +
                    "Consider taking a break and practicing mindfulness.");
            statusLabel.setText("⚠️ Stress detected!");
        } else {
            remindersArea.setText("✅ No stress indicators detected.\n" +
                    "Keep maintaining your positive mood!");
            statusLabel.setText("✅ All clear!");
        }
    }

    private void showSnoozeSettings() {
        if (reminderManager == null) {
            statusLabel.setText("❌ Services not initialized.");
            return;
        }
        remindersArea.setText("🔇 Snooze Settings:\n\n" +
                "Reminders can be snoozed for 10 minutes.\n" +
                "Snoozed reminders will be suppressed\n" +
                "until the snooze window expires.\n\n" +
                "This prevents notification spam\n" +
                "while allowing you to stay focused.");
        statusLabel.setText("ℹ️ Snooze settings displayed.");
    }

    private void refresh() {
        remindersArea.setText("Refreshing...");
        javafx.application.Platform.runLater(() -> {
            showPendingHabits();
            statusLabel.setText("✅ Refreshed.");
            com.mindbloom.ui.AnimationUtil.fadeInScale(remindersArea, 300);
        });
    }
}
