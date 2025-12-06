package com.mindbloom.service;

import com.mindbloom.model.MoodEntry;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;


public class MoodAnalytics {
    private final List<MoodEntry> moodEntries;

    
    public MoodAnalytics(List<MoodEntry> moodEntries) {
        this.moodEntries = moodEntries != null ? moodEntries : new ArrayList<>();
    }

    
    public String getMostFrequentMood() {
        if (moodEntries.isEmpty()) {
            return null;
        }
        return moodEntries.stream()
                .collect(Collectors.groupingBy(MoodEntry::getMood, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    
    public Map<String, Integer> getMoodFrequency() {
        return moodEntries.stream()
                .collect(Collectors.groupingBy(MoodEntry::getMood, Collectors.summingInt(e -> 1)))
                .entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
    }

    
    public Map<String, Integer> getWeeklyMoodFrequency() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate weekEnd = weekStart.plusDays(6);
        
        return moodEntries.stream()
                .filter(e -> !e.getDate().isBefore(weekStart) && !e.getDate().isAfter(weekEnd))
                .collect(Collectors.groupingBy(MoodEntry::getMood, Collectors.summingInt(e -> 1)))
                .entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
    }

    
    public Map<String, Integer> getMonthlyMoodFrequency() {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.getMonth().length(today.isLeapYear()));
        
        return moodEntries.stream()
                .filter(e -> !e.getDate().isBefore(monthStart) && !e.getDate().isAfter(monthEnd))
                .collect(Collectors.groupingBy(MoodEntry::getMood, Collectors.summingInt(e -> 1)))
                .entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
    }

    
    public Map<String, Object> getBestWeek() {
        if (moodEntries.isEmpty()) {
            return null;
        }

        
        Map<String, List<MoodEntry>> weeklyGroups = new LinkedHashMap<>();
        for (MoodEntry entry : moodEntries) {
            LocalDate date = entry.getDate();
            LocalDate weekStart = date.minusDays(date.getDayOfWeek().getValue() - 1);
            String weekKey = weekStart.toString();
            weeklyGroups.computeIfAbsent(weekKey, k -> new ArrayList<>()).add(entry);
        }

        
        Map<String, Integer> weekScores = new HashMap<>();
        Map<String, String> weekDates = new HashMap<>();
        
        for (Map.Entry<String, List<MoodEntry>> week : weeklyGroups.entrySet()) {
            String weekKey = week.getKey();
            List<MoodEntry> entries = week.getValue();
            LocalDate weekStart = LocalDate.parse(weekKey);
            LocalDate weekEnd = weekStart.plusDays(6);
            weekDates.put(weekKey, weekStart + " to " + weekEnd);
            
            int score = 0;
            for (MoodEntry entry : entries) {
                String mood = entry.getMood().toLowerCase();
                if (mood.contains("happy") || mood.contains("joyful")) score += 3;
                else if (mood.contains("calm") || mood.contains("peaceful")) score += 2;
                else if (mood.contains("tired")) score += 1;
                else score += 0;
            }
            weekScores.put(weekKey, score);
        }

        
        String bestWeekKey = weekScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        if (bestWeekKey == null) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("week", weekDates.get(bestWeekKey));
        result.put("score", weekScores.get(bestWeekKey));
        result.put("count", weeklyGroups.get(bestWeekKey).size());
        return result;
    }

    
    public String generateMoodBarChart(Map<String, Integer> frequency) {
        if (frequency.isEmpty()) {
            return "No data to display.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n📊 Mood Frequency Chart:\n");

        
        int maxCount = frequency.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        int chartWidth = 30;

        for (Map.Entry<String, Integer> entry : frequency.entrySet()) {
            String mood = entry.getKey();
            int count = entry.getValue();
            int barLength = (count * chartWidth) / maxCount;
            
            sb.append(String.format("  %-12s │", mood));
            sb.append("█".repeat(barLength));
            sb.append(String.format(" %d\n", count));
        }

        return sb.toString();
    }

    
    public String generateWeeklyTrendChart() {
        LocalDate today = LocalDate.now();
        StringBuilder sb = new StringBuilder();
        sb.append("\n📈 Weekly Trend (Last 4 Weeks):\n");

        for (int week = 3; week >= 0; week--) {
            LocalDate weekStart = today.minusWeeks(week).minusDays(
                    today.minusWeeks(week).getDayOfWeek().getValue() - 1);
            LocalDate weekEnd = weekStart.plusDays(6);

            Map<String, Integer> weekFreq = moodEntries.stream()
                    .filter(e -> !e.getDate().isBefore(weekStart) && !e.getDate().isAfter(weekEnd))
                    .collect(Collectors.groupingBy(MoodEntry::getMood, Collectors.summingInt(e -> 1)));

            int score = 0;
            for (Map.Entry<String, Integer> mood : weekFreq.entrySet()) {
                String moodStr = mood.getKey().toLowerCase();
                int count = mood.getValue();
                if (moodStr.contains("happy") || moodStr.contains("joyful")) score += count * 3;
                else if (moodStr.contains("calm")) score += count * 2;
                else if (moodStr.contains("tired")) score += count * 1;
            }

            
            int displayScore = Math.min(10, score / Math.max(1, weekFreq.size()));
            
            sb.append(String.format("  Week %d: ", 4 - week));
            sb.append("█".repeat(displayScore));
            sb.append("░".repeat(10 - displayScore));
            sb.append(String.format(" (Score: %d)\n", score));
        }

        return sb.toString();
    }

    
    public String getAnalyticsSummary() {
        if (moodEntries.isEmpty()) {
            return "📭 No mood data yet. Start tracking to see analytics!";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n").append("=".repeat(50)).append("\n");
        sb.append("🧠 MOOD ANALYTICS DASHBOARD\n");
        sb.append("=".repeat(50)).append("\n");

        
        sb.append(String.format("\n📊 Overall Statistics:\n"));
        sb.append(String.format("  Total moods recorded: %d\n", moodEntries.size()));
        String mostFrequent = getMostFrequentMood();
        if (mostFrequent != null) {
            sb.append(String.format("  Most frequent mood: %s\n", mostFrequent));
        }

        
        sb.append("\n📅 This Week's Breakdown:");
        Map<String, Integer> weeklyFreq = getWeeklyMoodFrequency();
        if (weeklyFreq.isEmpty()) {
            sb.append(" No entries this week.\n");
        } else {
            sb.append(generateMoodBarChart(weeklyFreq));
        }

        sb.append("\n📅 This Month's Breakdown:");
        Map<String, Integer> monthlyFreq = getMonthlyMoodFrequency();
        if (monthlyFreq.isEmpty()) {
            sb.append(" No entries this month.\n");
        } else {
            sb.append(generateMoodBarChart(monthlyFreq));
        }

        
        Map<String, Object> bestWeek = getBestWeek();
        if (bestWeek != null) {
            sb.append(String.format("\n⭐ Your Best Week:\n"));
            sb.append(String.format("  Period: %s\n", bestWeek.get("week")));
            sb.append(String.format("  Entries: %d\n", bestWeek.get("count")));
        }

        
        sb.append(generateWeeklyTrendChart());

        sb.append("\n" + "=".repeat(50) + "\n");
        return sb.toString();
    }
}
