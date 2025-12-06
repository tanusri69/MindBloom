package com.mindbloom.model;

import java.io.Serializable;


public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String passwordHash;

    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    @Override
    public String toString() {
        return username;
    }
}
