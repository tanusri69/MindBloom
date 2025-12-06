package com.mindbloom.ui;

import com.mindbloom.motivation.QuotesManager;
import com.mindbloom.service.MoodTrackerService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.mindbloom.ui.AnimationUtil;


public class QuotesController {
    @FXML
    private Button randomQuoteButton;
    @FXML
    private Button quoteOfDayButton;
    @FXML
    private Button favoritesButton;
    @FXML
    private Button markFavoriteButton;
    @FXML
    private ComboBox<String> categoryCombo;
    @FXML
    private Button categoryQuoteButton;
    @FXML
    private TextArea quoteArea;
    @FXML
    private Label statusLabel;

    private QuotesManager quotesManager;
    private String currentQuote = "";

    @FXML
    public void initialize() {
        quotesManager = new QuotesManager();

        categoryCombo.setItems(FXCollections.observableArrayList(
                "Focus", "Positivity", "Discipline", "Stress Relief"
        ));

        randomQuoteButton.setOnAction(event -> displayRandomQuote());
        quoteOfDayButton.setOnAction(event -> displayQuoteOfDay());
        favoritesButton.setOnAction(event -> displayFavorites());
        markFavoriteButton.setOnAction(event -> markCurrentAsFavorite());
        categoryQuoteButton.setOnAction(event -> displayCategoryQuote());

        
        AnimationUtil.fadeIn(quoteArea);
        AnimationUtil.pulse(randomQuoteButton);
    }

    private void displayRandomQuote() {
        currentQuote = quotesManager.getRandomQuote();
        quoteArea.setText(currentQuote);
        statusLabel.setText("✅ Random quote displayed.");
        com.mindbloom.ui.AnimationUtil.fadeInScale(quoteArea, 300);
    }

    private void displayQuoteOfDay() {
        currentQuote = quotesManager.getQuoteOfTheDay();
        quoteArea.setText(currentQuote);
        statusLabel.setText("✅ Quote of the day displayed.");
        com.mindbloom.ui.AnimationUtil.fadeInScale(quoteArea, 300);
    }

    private void displayFavorites() {
        StringBuilder sb = new StringBuilder();
        try {
            for (String fav : quotesManager.getFavorites()) {
                sb.append("⭐ ").append(fav).append("\n\n");
            }
            if (sb.length() == 0) {
                quoteArea.setText("No favorite quotes yet!");
                statusLabel.setText("ℹ️ No favorites.");
            } else {
                quoteArea.setText(sb.toString());
                statusLabel.setText("✅ Displaying " + quotesManager.getFavorites().size() + " favorites.");
            }
        } catch (Exception e) {
            statusLabel.setText("❌ Error loading favorites.");
        }
    }

    private void markCurrentAsFavorite() {
        if (currentQuote.isEmpty()) {
            statusLabel.setText("❌ No quote to mark. Display a quote first.");
            com.mindbloom.ui.AnimationUtil.shake(quoteArea);
            return;
        }
        try {
            quotesManager.addFavorite(currentQuote);
            statusLabel.setText("✅ Quote marked as favorite!");
        } catch (Exception e) {
            statusLabel.setText("❌ Error marking favorite: " + e.getMessage());
        }
    }

    private void displayCategoryQuote() {
        String category = categoryCombo.getValue();
        if (category == null) {
            statusLabel.setText("❌ Please select a category.");
            return;
        }
        currentQuote = quotesManager.getRandomQuoteByCategory(category);
        quoteArea.setText(currentQuote);
        statusLabel.setText("✅ Quote from '" + category + "' displayed.");
        com.mindbloom.ui.AnimationUtil.fadeInScale(quoteArea, 300);
    }
}
