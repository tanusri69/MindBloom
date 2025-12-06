package com.mindbloom.service;

import com.mindbloom.model.User;
import java.io.*;
import java.nio.file.*;
import java.util.*;


public class UserService {
    private static final String USERS_FILE = "users.dat";
    private Map<String, String> users = new HashMap<>(); 

    public UserService() {
        loadUsers();
    }

    
    private void loadUsers() {
        File f = new File(USERS_FILE);
        if (!f.exists()) {
            
            users.put("admin", hashPassword("admin"));
            saveUsers();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            @SuppressWarnings("unchecked")
            Map<String, String> loaded = (Map<String, String>) ois.readObject();
            users = loaded;
        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
            users.put("admin", hashPassword("admin"));
        }
    }

    
    private void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(users);
        } catch (Exception e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    
    public boolean authenticate(String username, String password) {
        if (username == null || password == null) return false;
        String hash = users.get(username);
        return hash != null && hash.equals(hashPassword(password));
    }

    
    public boolean register(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            return false;
        }
        if (users.containsKey(username)) {
            return false; 
        }
        users.put(username, hashPassword(password));
        saveUsers();
        return true;
    }

    
    private String hashPassword(String password) {
        return String.valueOf(password.hashCode());
    }
}
