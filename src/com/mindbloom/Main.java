package com.mindbloom;

import com.mindbloom.exceptions.InvalidMoodException;
import com.mindbloom.exceptions.HabitNotFoundException;
import com.mindbloom.model.Habit;
import com.mindbloom.service.HabitTrackerService;
import com.mindbloom.service.MoodTrackerService;
import com.mindbloom.service.StorageService;
import com.mindbloom.service.MoodAnalytics;
import com.mindbloom.service.ReminderManager;
import com.mindbloom.motivation.MotivationEngine;
import com.mindbloom.motivation.QuotesManager;
import com.mindbloom.threads.AutoSaveThread;
import com.mindbloom.threads.ReminderThread;

import java.time.LocalDate;
import java.util.Scanner;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;


public class Main {
    private static final MoodTrackerService moodService = new MoodTrackerService();
    private static final HabitTrackerService habitService = new HabitTrackerService();
    private static final MotivationEngine motivationEngine = new MotivationEngine();
    private static final QuotesManager quotesManager = new QuotesManager();
    private static final Scanner scanner = new Scanner(System.in);
    private static AutoSaveThread autosaveThread;
    private static ReminderThread reminderThread;

    private static final String STORAGE_FILE = "mindbloom.dat";
    private static final String MENU_DIVIDER = "\n=== 🌱 MindBloom Console ===";

    
    public static void main(String[] args) {
        System.out.println("🌱 Welcome to MindBloom!");
        
        
        String quoteOfDay = quotesManager.getQuoteOfTheDay();
        if (quoteOfDay != null) {
            System.out.println("\n📖 Quote of the Day:");
            System.out.println("  \"" + quoteOfDay + "\"\n");
        }
        
        initializeApplication();
        runMainMenuLoop();
    }

    
    private static void initializeApplication() {
        autosaveThread = new AutoSaveThread(moodService, habitService);
        autosaveThread.start();
        
        reminderThread = new ReminderThread(habitService, moodService);
        reminderThread.start();
    }

    
    private static void runMainMenuLoop() {
        while (true) {
            displayMainMenu();
            processUserChoice(getUserChoice());
        }
    }

    
    private static void displayMainMenu() {
        System.out.println(MENU_DIVIDER);
        System.out.println("1. Add Mood");
        System.out.println("2. View Moods");
        System.out.println("3. Add Habit");
        System.out.println("4. View Habits");
        System.out.println("5. Save Data");
        System.out.println("6. Load Data");
        System.out.println("7. Exit");
        System.out.println("8. Tick Habit");
        System.out.println("9. Show Motivation");
        System.out.println("10. Random Quote");
        System.out.println("11. View Favorites");
        System.out.println("12. Mark as Favorite");
        System.out.println("13. Quote by Category");
        System.out.println("14. Analytics Dashboard");
        System.out.println("15. Reminder Settings");
    }

    
    private static int getUserChoice() {
        System.out.print("Enter choice (1-9): ");
        try {
            String input = scanner.nextLine().trim();
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid input. Please enter a number between 1 and 9.");
            return 0;
        }
    }

    
    private static void processUserChoice(int choice) {
        switch (choice) {
            case 1 -> addMood();
            case 2 -> viewMoods();
            case 3 -> addHabit();
            case 4 -> viewHabits();
            case 5 -> saveData();
            case 6 -> loadData();
            case 7 -> exitApp();
            case 8 -> tickHabit();
            case 9 -> showMotivation();
            case 10 -> showRandomQuote();
            case 11 -> showFavorites();
            case 12 -> markFavorite();
            case 13 -> showQuoteByCategory();
            case 14 -> showAnalyticsDashboard();
            case 15 -> showReminderSettings();
            default -> System.out.println("❌ Invalid choice! Please select a number between 1 and 15.");
        }
    }

    
    private static void addMood() {
        int attempts = 0;
        while (attempts < 3) {
            System.out.print("Enter mood (e.g., happy, sad, calm) or 'q' to cancel: ");
            String mood = scanner.nextLine().trim();
            if (mood.equalsIgnoreCase("q")) {
                System.out.println("Cancelled.");
                return;
            }
            System.out.print("Note (optional, press Enter to skip): ");
            String note = scanner.nextLine().trim();
            try {
                moodService.addEntry(LocalDate.now(), mood, note.isEmpty() ? "" : note);
                System.out.println("✅ Mood added!");
                return;
            } catch (InvalidMoodException e) {
                attempts++;
                System.out.println("⚠️ " + e.getMessage());
                if (attempts < 3) {
                    System.out.println("Please try again (" + (3 - attempts) + " attempts left).");
                } else {
                    System.out.println("Too many invalid attempts; returning to menu.");
                }
            }
        }
    }

    
    private static void viewMoods() {
        if (moodService.getAll().isEmpty()) {
            System.out.println("📭 No mood records yet. Add one to get started!");
        } else {
            System.out.println("\n📋 Your Mood History:");
            moodService.getAll().forEach(m -> System.out.println("  " + m));
        }
    }

    
    private static void addHabit() {
        System.out.print("Enter habit name (e.g., exercise, read, meditate): ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("❌ Habit name cannot be empty.");
            return;
        }
        habitService.addHabit(name);
        System.out.println("✅ Habit added!");
    }

    
    private static void viewHabits() {
        if (habitService.getAll().isEmpty()) {
            System.out.println("📭 No habits yet. Add one to get started!");
        } else {
            System.out.println("\n📋 Your Habits:");
            habitService.getAll().forEach(h -> 
                System.out.println("  • " + h.getName() + " | 🔥 Streak: " + h.getStreak())
            );
        }
    }

    
    private static void saveData() {
        try {
            StorageService.saveToFile(STORAGE_FILE, moodService.getAll(), habitService.getAll());
            System.out.println("✅ Data saved successfully to " + STORAGE_FILE);
        } catch (Exception e) {
            System.out.println("❌ Error saving file: " + e.getMessage());
        }
    }

    
    private static void loadData() {
        try {
            StorageService.loadFromFile(STORAGE_FILE, moodService, habitService);
            System.out.println("✅ Data loaded successfully from " + STORAGE_FILE);
        } catch (Exception e) {
            System.out.println("❌ Error loading file: " + e.getMessage());
        }
    }

    
    private static void tickHabit() {
        try {
            System.out.print("Enter habit to mark as done: ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                System.out.println("❌ Habit name cannot be empty.");
                return;
            }
            habitService.tickHabit(name);
            System.out.println("✅ Habit streak increased!");
        } catch (HabitNotFoundException e) {
            System.out.println("⚠️ Error: " + e.getMessage());
        }
    }

    
    private static void showMotivation() {
        System.out.print("Enter your name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            name = "Friend";
        }

        
        String latestMood = moodService.getLatestMood();
        String moodToUse = null;

        if (latestMood != null) {
            System.out.print("Use your latest mood ('" + latestMood + "') for a tailored message? (Y/n): ");
            String useLatest = scanner.nextLine().trim();
            if (useLatest.isEmpty() || useLatest.equalsIgnoreCase("y") || useLatest.equalsIgnoreCase("yes")) {
                moodToUse = latestMood;
            }
        }

        if (moodToUse == null) {
            System.out.print("Enter your current mood for a tailored message (or press Enter to skip): ");
            String inputMood = scanner.nextLine().trim();
            if (!inputMood.isEmpty()) {
                moodToUse = inputMood;
            }
        }

        System.out.println();
        if (moodToUse == null) {
            motivationEngine.showMotivation(name);
        } else {
            motivationEngine.showMotivation(name, moodToUse);
        }
    }

    
    private static void showRandomQuote() {
        String quote = quotesManager.getRandomQuote();
        if (quote == null) {
            System.out.println("📭 No quotes available.");
            return;
        }
        System.out.println("\n📖 Random Quote:");
        System.out.println("  \"" + quote + "\"");
        
        
        System.out.print("Add to favorites? (Y/n): ");
        String response = scanner.nextLine().trim();
        if (response.isEmpty() || response.equalsIgnoreCase("y") || response.equalsIgnoreCase("yes")) {
            quotesManager.addFavorite(quote);
            System.out.println("✅ Added to favorites!");
        }
        System.out.println();
    }

    
    private static void showFavorites() {
        List<String> favorites = quotesManager.getFavorites();
        if (favorites.isEmpty()) {
            System.out.println("📭 No favorite quotes yet. Start adding some!");
            return;
        }
        System.out.println("\n💖 Your Favorite Quotes:");
        for (int i = 0; i < favorites.size(); i++) {
            System.out.println("  " + (i + 1) + ". \"" + favorites.get(i) + "\"");
        }
        System.out.println();
    }

    
    private static void markFavorite() {
        System.out.print("Enter the quote you'd like to save (or press Enter to get a random one): ");
        String quote = scanner.nextLine().trim();
        
        if (quote.isEmpty()) {
            quote = quotesManager.getRandomQuote();
            if (quote == null) {
                System.out.println("📭 No quotes available.");
                return;
            }
            System.out.println("Random quote: \"" + quote + "\"");
        }
        
        if (quotesManager.isFavorite(quote)) {
            System.out.println("⚠️ This quote is already in favorites.");
        } else {
            quotesManager.addFavorite(quote);
            System.out.println("✅ Quote added to favorites!");
        }
        System.out.println();
    }

    
    private static void showQuoteByCategory() {
        Set<String> categories = quotesManager.getCategories();
        if (categories.isEmpty()) {
            System.out.println("📭 No categories available.");
            return;
        }
        
        System.out.println("\n📚 Available Categories:");
        List<String> catList = new ArrayList<>(categories);
        for (int i = 0; i < catList.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + catList.get(i));
        }
        
        System.out.print("Choose a category (1-" + catList.size() + "): ");
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            if (choice < 1 || choice > catList.size()) {
                System.out.println("❌ Invalid choice.");
                return;
            }
            String category = catList.get(choice - 1);
            String quote = quotesManager.getRandomQuoteByCategory(category);
            if (quote == null) {
                System.out.println("📭 No quotes in this category.");
                return;
            }
            System.out.println("\n📖 Quote from " + category + ":");
            System.out.println("  \"" + quote + "\"");
            
            
            System.out.print("Add to favorites? (Y/n): ");
            String response = scanner.nextLine().trim();
            if (response.isEmpty() || response.equalsIgnoreCase("y") || response.equalsIgnoreCase("yes")) {
                quotesManager.addFavorite(quote);
                System.out.println("✅ Added to favorites!");
            }
            System.out.println();
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid input. Please enter a number.");
        }
    }

    
    private static void showAnalyticsDashboard() {
        MoodAnalytics analytics = new MoodAnalytics(moodService.getAll());
        System.out.println(analytics.getAnalyticsSummary());
    }

    
    private static void showReminderSettings() {
        if (reminderThread == null) {
            System.out.println("⚠️ Reminders are not active. Restart the app to enable.");
            return;
        }

        System.out.println("\n" + "=".repeat(50));
        System.out.println("🔔 REMINDER SETTINGS");
        System.out.println("=".repeat(50));
        System.out.println("1. View pending habit reminders");
        System.out.println("2. Snooze a habit reminder");
        System.out.println("3. Check stress detection");
        System.out.println("4. Back to main menu");
        System.out.print("Choose an option (1-4): ");

        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            switch (choice) {
                case 1 -> viewPendingReminders();
                case 2 -> snoozeReminderOption();
                case 3 -> checkStressDetection();
                case 4 -> {} 
                default -> System.out.println("❌ Invalid choice.");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid input.");
        }
        System.out.println();
    }

    
    private static void viewPendingReminders() {
        System.out.println("\n📋 Pending Habit Reminders:");
        if (habitService.getAll().isEmpty()) {
            System.out.println("  No habits tracked yet.");
        } else {
            for (Habit habit : habitService.getAll()) {
                System.out.println("  🔔 " + habit.getName() + " (Streak: " + habit.getStreak() + ")");
            }
        }
    }

    
    private static void snoozeReminderOption() {
        System.out.print("Enter habit name to snooze for 10 minutes: ");
        String habitName = scanner.nextLine().trim();
        if (habitName.isEmpty()) {
            System.out.println("❌ Habit name cannot be empty.");
            return;
        }
        reminderThread.snoozeHabitReminder(habitName);
        System.out.println("✅ Reminder for '" + habitName + "' snoozed for 10 minutes.");
    }

    
    private static void checkStressDetection() {
        String latestMood = moodService.getLatestMood();
        if (latestMood == null) {
            System.out.println("📭 No mood recorded yet. Start tracking to enable stress detection.");
            return;
        }

        System.out.println("\n🧠 Stress Detection Analysis:");
        System.out.println("  Latest mood: " + latestMood);

        String moodLower = latestMood.toLowerCase();
        if (moodLower.contains("anxious") || moodLower.contains("stress") || 
            moodLower.contains("angry") || moodLower.contains("sad") ||
            moodLower.contains("worried") || moodLower.contains("nervous")) {
            System.out.println("  Status: ⚠️ Stress Detected");
            System.out.println("  Recommendation: Take a break, breathe deeply, or chat with someone.");
        } else {
            System.out.println("  Status: ✅ No stress detected");
        }
    }

    
    private static void exitApp() {
        System.out.println("\n🌱 Thank you for using MindBloom! Saving data...");
        if (autosaveThread != null) {
            autosaveThread.stopAutoSave();
            try {
                autosaveThread.join(2000);
            } catch (InterruptedException ignored) {
                
            }
        }
        saveData();
        System.out.println("✅ Goodbye! Keep taking care of yourself.");
        System.exit(0);
    }

}
