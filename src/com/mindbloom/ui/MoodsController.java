package com.mindbloom.ui;

import com.mindbloom.model.MoodEntry;
import com.mindbloom.service.MoodTrackerService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.mindbloom.ui.AnimationUtil;
import java.time.LocalDate;


public class MoodsController {
    @FXML
    private ComboBox<String> moodComboBox;
    @FXML
    private TextField noteTextField;
    @FXML
    private Button addMoodButton;
    @FXML
    private Button refreshButton;
    @FXML
    private TableView<MoodEntry> moodHistoryTable;
    @FXML
    private TableColumn<MoodEntry, LocalDate> dateColumn;
    @FXML
    private TableColumn<MoodEntry, String> moodColumn;
    @FXML
    private TableColumn<MoodEntry, String> noteColumn;
    @FXML
    private Label statusLabel;

    private MoodTrackerService moodService;

    
    @FXML
    public void initialize() {
        
        moodComboBox.setItems(FXCollections.observableArrayList(
                "happy", "sad", "calm", "anxious", "energetic", "tired", "angry", "grateful"
        ));
        moodComboBox.setEditable(true);

        
        dateColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getDate()));
        moodColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMood()));
        noteColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNote()));

        
        addMoodButton.setOnAction(event -> addMood());
        refreshButton.setOnAction(event -> refreshMoodHistory());

        
        AnimationUtil.fadeIn(moodHistoryTable);
        AnimationUtil.pulse(addMoodButton);
    }

    
    public void setMoodService(MoodTrackerService moodService) {
        this.moodService = moodService;
        refreshMoodHistory();
    }

    
    private void addMood() {
        String mood = moodComboBox.getValue();
        String note = noteTextField.getText();

        if (mood == null || mood.trim().isEmpty()) {
            statusLabel.setText("❌ Please select or enter a mood.");
            com.mindbloom.ui.AnimationUtil.shake(moodComboBox);
            return;
        }

        try {
            moodService.addMood(mood.trim(), note);
            statusLabel.setText("✅ Mood added successfully!");
            noteTextField.clear();
            moodComboBox.setValue(null);
            refreshMoodHistory();
        } catch (Exception e) {
            statusLabel.setText("❌ " + e.getMessage());
        }
    }

    
    private void refreshMoodHistory() {
        if (moodService == null) return;
        moodHistoryTable.setItems(FXCollections.observableArrayList(
                moodService.getAll()
        ));
        
        com.mindbloom.ui.AnimationUtil.fadeInScale(moodHistoryTable, 350);
    }
}
