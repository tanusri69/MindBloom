package com.mindbloom.motivation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;


public class QuotesManager {
    private static final String QUOTES_FILE = "quotes.txt";
    private static final String FAVORITES_FILE = "favorites.txt";

    private Map<String, List<String>> quotesByCategory = new HashMap<>();
    private Set<String> favorites = new HashSet<>();
    private Random random = new Random();

    
    public QuotesManager() {
        loadQuotes();
        loadFavorites();
    }

    
    private void loadQuotes() {
        File file = new File(QUOTES_FILE);
        if (!file.exists()) {
            createDefaultQuotesFile();
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue; 
                }
                String[] parts = line.split("\\|", 2);
                if (parts.length == 2) {
                    String category = parts[0].trim();
                    String quote = parts[1].trim();
                    quotesByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(quote);
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Error loading quotes: " + e.getMessage());
        }
    }

    
    private void loadFavorites() {
        File file = new File(FAVORITES_FILE);
        if (!file.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    favorites.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("⚠️ Error loading favorites: " + e.getMessage());
        }
    }

    
    private void saveFavorites() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FAVORITES_FILE))) {
            for (String fav : favorites) {
                writer.println(fav);
            }
        } catch (IOException e) {
            System.err.println("❌ Error saving favorites: " + e.getMessage());
        }
    }

    
    public String getQuoteOfTheDay() {
        List<String> allQuotes = getAllQuotes();
        if (allQuotes.isEmpty()) {
            return null;
        }
        int dayOfYear = LocalDate.now().getDayOfYear();
        return allQuotes.get(dayOfYear % allQuotes.size());
    }

    
    public String getRandomQuote() {
        List<String> allQuotes = getAllQuotes();
        if (allQuotes.isEmpty()) {
            return null;
        }
        return allQuotes.get(random.nextInt(allQuotes.size()));
    }

    
    public String getRandomQuoteByCategory(String category) {
        List<String> quotes = quotesByCategory.get(category);
        if (quotes == null || quotes.isEmpty()) {
            return null;
        }
        return quotes.get(random.nextInt(quotes.size()));
    }

    
    public List<String> getAllQuotes() {
        List<String> all = new ArrayList<>();
        for (List<String> quotes : quotesByCategory.values()) {
            all.addAll(quotes);
        }
        return all;
    }

    
    public Set<String> getCategories() {
        return quotesByCategory.keySet();
    }

    
    public void addFavorite(String quote) {
        if (quote != null && !quote.isEmpty()) {
            favorites.add(quote);
            saveFavorites();
        }
    }

    
    public void removeFavorite(String quote) {
        if (favorites.remove(quote)) {
            saveFavorites();
        }
    }

    
    public List<String> getFavorites() {
        return new ArrayList<>(favorites);
    }

    
    public void persistFavorites() {
        saveFavorites();
    }

    
    public List<String> getQuotesForCategory(String category) {
        List<String> q = quotesByCategory.get(category);
        return q == null ? java.util.Collections.emptyList() : new ArrayList<>(q);
    }

    
    public boolean isFavorite(String quote) {
        return favorites.contains(quote);
    }

    
    public String getRandomFavorite() {
        if (favorites.isEmpty()) {
            return null;
        }
        List<String> favList = new ArrayList<>(favorites);
        return favList.get(random.nextInt(favList.size()));
    }

    
    private void createDefaultQuotesFile() {
        String defaultQuotes = "# MindBloom Quotes - Add your own below!\n" +
                "# Format: CATEGORY | Quote text\n" +
                "\n" +
                "Focus | Success is the sum of small efforts repeated day in and day out.\n" +
                "Focus | The secret of getting ahead is getting started.\n" +
                "Focus | Don't watch the clock; do what it does. Keep going.\n" +
                "Focus | Your focus determines your reality.\n" +
                "Focus | One step at a time is good walking.\n" +
                "\n" +
                "Positivity | Every day may not be good, but there is something good in every day.\n" +
                "Positivity | Your time is limited, don't waste it living someone else's life.\n" +
                "Positivity | The only way to do great work is to love what you do.\n" +
                "Positivity | Believe you can and you're halfway there.\n" +
                "Positivity | Happiness is not by chance, but by choice.\n" +
                "\n" +
                "Discipline | Discipline is choosing between what you want now and what you want most.\n" +
                "Discipline | The only way to build true discipline is through practice.\n" +
                "Discipline | Excellence is not a skill, it's a habit.\n" +
                "Discipline | Small daily improvements are the key to success.\n" +
                "Discipline | Your future self will thank you for what you do today.\n" +
                "\n" +
                "Stress Relief | Breathe. Let go. And remind yourself that this too shall pass.\n" +
                "Stress Relief | You are braver than you believe, stronger than you seem, and smarter than you think.\n" +
                "Stress Relief | Anxiety is temporary; your strength is permanent.\n" +
                "Stress Relief | Peace comes from within. Do not seek it without.\n" +
                "Stress Relief | Take care of your body. It's the only place you have to live.\n";

        try (PrintWriter writer = new PrintWriter(new FileWriter(QUOTES_FILE))) {
            writer.write(defaultQuotes);
        } catch (IOException e) {
            System.err.println("❌ Error creating default quotes file: " + e.getMessage());
        }
    }
}
