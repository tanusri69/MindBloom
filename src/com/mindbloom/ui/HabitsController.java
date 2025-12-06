package com.mindbloom.ui;

import com.mindbloom.model.Habit;
import com.mindbloom.service.HabitTrackerService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.mindbloom.ui.AnimationUtil;
import java.time.LocalDate;


public class HabitsController {
    @FXML
    private TextField habitNameField;
    @FXML
    private ComboBox<String> frequencyCombo;
    @FXML
    private Button addHabitButton;
    @FXML
    private Button refreshButton;
    @FXML
    private TableView<Habit> habitsTable;
    @FXML
    private TableColumn<Habit, String> habitColumn;
    @FXML
    private TableColumn<Habit, String> frequencyColumn;
    @FXML
    private TableColumn<Habit, Integer> streakColumn;
    @FXML
    private TableColumn<Habit, LocalDate> lastCompletedColumn;
    @FXML
    private TableColumn<Habit, String> actionsColumn;
    @FXML
    private Label statusLabel;

    private HabitTrackerService habitService;

    @FXML
    public void initialize() {
        frequencyCombo.setItems(FXCollections.observableArrayList(
                "Daily", "Weekly", "Monthly"
        ));

        habitColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        frequencyColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty("Daily"));
        streakColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getStreak()).asObject());
        lastCompletedColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getLastCompleted()));

        addHabitButton.setOnAction(event -> addHabit());
        refreshButton.setOnAction(event -> refreshHabits());

        
        AnimationUtil.fadeIn(habitsTable);
        AnimationUtil.pulse(addHabitButton);
    }

    public void setHabitService(HabitTrackerService habitService) {
        this.habitService = habitService;
        refreshHabits();
    }

    private void addHabit() {
        String name = habitNameField.getText();

        if (name == null || name.trim().isEmpty()) {
            statusLabel.setText("❌ Please enter a habit name.");
            com.mindbloom.ui.AnimationUtil.shake(habitNameField);
            return;
        }

        try {
            habitService.addHabit(name.trim());
            statusLabel.setText("✅ Habit added successfully!");
            habitNameField.clear();
            frequencyCombo.setValue(null);
            refreshHabits();
        } catch (Exception e) {
            statusLabel.setText("❌ Error adding habit: " + e.getMessage());
        }
    }

    private void refreshHabits() {
        if (habitService == null) return;
        habitsTable.setItems(FXCollections.observableArrayList(
                habitService.getAll()
        ));
        
        com.mindbloom.ui.AnimationUtil.fadeInScale(habitsTable, 350);
    }
}
