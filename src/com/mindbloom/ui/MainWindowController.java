package com.mindbloom.ui;

import com.mindbloom.service.HabitTrackerService;
import com.mindbloom.service.MoodTrackerService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import com.mindbloom.ui.AnimationUtil;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class MainWindowController {
    @FXML
    private TabPane mainTabPane;
    @FXML
    private Label statusLabel;
    @FXML
    private Label dateTimeLabel;

    private MoodTrackerService moodService;
    private HabitTrackerService habitService;

    
    @FXML
    public void initialize() {
        
        updateDateTime();
        Thread dateTimeThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    updateDateTime();
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        dateTimeThread.setDaemon(true);
        dateTimeThread.start();
        
        AnimationUtil.fadeIn(dateTimeLabel);
        AnimationUtil.fadeIn(statusLabel);
        AnimationUtil.slideInFromLeft(mainTabPane);
    }

    
    public void setServices(MoodTrackerService moodService, HabitTrackerService habitService) {
        this.moodService = moodService;
        this.habitService = habitService;
        
        
        initializeTabControllers();
    }

    
    private void initializeTabControllers() {
        
        
    }

    
    private void updateDateTime() {
        String dateTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("EEE, MMM dd yyyy | HH:mm:ss"));
        javafx.application.Platform.runLater(() -> dateTimeLabel.setText(dateTime));
    }

    
    public void setStatus(String message) {
        statusLabel.setText(message);
    }

    
    public MoodTrackerService getMoodService() {
        return moodService;
    }

    
    public HabitTrackerService getHabitService() {
        return habitService;
    }
}
