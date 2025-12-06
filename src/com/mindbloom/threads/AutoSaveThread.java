package com.mindbloom.threads;

import com.mindbloom.service.MoodTrackerService;
import com.mindbloom.service.HabitTrackerService;
import com.mindbloom.service.StorageService;


public class AutoSaveThread extends Thread {
    private static final long SAVE_INTERVAL_MS = 30_000; 
    private static final String AUTOSAVE_FILE = "autosave.dat";

    private final MoodTrackerService moodService;
    private final HabitTrackerService habitService;
    private final java.util.List<Runnable> extraSaveActions = new java.util.ArrayList<>();
    private volatile boolean running = true;

    
    public AutoSaveThread(MoodTrackerService moodService, HabitTrackerService habitService) {
        this.moodService = moodService;
        this.habitService = habitService;
        setDaemon(true);
        setName("MindBloom-AutoSaveThread");
    }

    
    public synchronized void saveData() {
        try {
            StorageService.saveToFile(AUTOSAVE_FILE,
                    moodService.getAll(),
                    habitService.getAll());
            System.out.println("💾 Auto-saved successfully!");
            
            for (Runnable r : new java.util.ArrayList<>(extraSaveActions)) {
                try { r.run(); } catch (Exception e) { System.out.println("⚠️ Extra auto-save action failed: " + e.getMessage()); }
            }
        } catch (Exception e) {
            System.out.println("⚠️ Auto-save failed: " + e.getMessage());
        }
    }

    
    public synchronized void addSaveAction(Runnable r) {
        if (r != null) extraSaveActions.add(r);
    }

    
    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(SAVE_INTERVAL_MS);
                if (running) {
                    saveData();
                }
            } catch (InterruptedException ignored) {
                
            }
        }
    }

    
    public void stopAutoSave() {
        running = false;
        interrupt();
    }
}
