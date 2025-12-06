package com.mindbloom.ui;

import com.mindbloom.service.MoodAnalytics;
import com.mindbloom.service.MoodTrackerService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import com.mindbloom.ui.AnimationUtil;
import java.util.Map;


public class AnalyticsController {
    @FXML
    private Button weeklyTrendButton;
    @FXML
    private Button monthlyTrendButton;
    @FXML
    private Button moodFreqButton;
    @FXML
    private Button bestWeekButton;
    @FXML
    private TextArea chartArea;
    @FXML
    private Label statusLabel;

    private MoodTrackerService moodService;
    private MoodAnalytics analytics;

    @FXML
    public void initialize() {
        weeklyTrendButton.setOnAction(event -> displayWeeklyTrend());
        monthlyTrendButton.setOnAction(event -> displayMonthlyTrend());
        moodFreqButton.setOnAction(event -> displayMoodFrequency());
        bestWeekButton.setOnAction(event -> displayBestWeek());

        
        AnimationUtil.fadeIn(chartArea);
        AnimationUtil.pulse(weeklyTrendButton);
    }

    public void setMoodService(MoodTrackerService moodService) {
        this.moodService = moodService;
        this.analytics = new MoodAnalytics(moodService.getAll());
        chartArea.setText("Analytics ready. Click a button to view data.");
    }

    private void displayWeeklyTrend() {
        if (analytics == null) {
            statusLabel.setText("❌ No data available.");
            return;
        }
        chartArea.setText(analytics.generateWeeklyTrendChart());
        statusLabel.setText("✅ Weekly trend displayed.");
        com.mindbloom.ui.AnimationUtil.fadeInScale(chartArea, 350);
    }

    private void displayMonthlyTrend() {
        if (analytics == null) {
            statusLabel.setText("❌ No data available.");
            return;
        }
        Map<String, Integer> frequency = analytics.getMonthlyMoodFrequency();
        chartArea.setText("📊 Monthly Mood Frequency:\n\n" + frequency.toString());
        statusLabel.setText("✅ Monthly trend displayed.");
        com.mindbloom.ui.AnimationUtil.fadeInScale(chartArea, 350);
    }

    private void displayMoodFrequency() {
        if (analytics == null) {
            statusLabel.setText("❌ No data available.");
            return;
        }
        Map<String, Integer> frequency = analytics.getMoodFrequency();
        chartArea.setText(analytics.generateMoodBarChart(frequency));
        statusLabel.setText("✅ Mood frequency displayed.");
        com.mindbloom.ui.AnimationUtil.fadeInScale(chartArea, 350);
    }

    private void displayBestWeek() {
        if (analytics == null) {
            statusLabel.setText("❌ No data available.");
            return;
        }
        Map<String, Object> bestWeek = analytics.getBestWeek();
        if (bestWeek == null) {
            chartArea.setText("No data available to determine best week.");
        } else {
            chartArea.setText("🏆 Best Week:\n" + bestWeek.toString());
        }
        statusLabel.setText("✅ Best week displayed.");
        com.mindbloom.ui.AnimationUtil.fadeInScale(chartArea, 350);
    }
}
