package com.mindbloom.exceptions;


public class InvalidMoodException extends Exception {
    
    
    public InvalidMoodException(String message) {
        super(message);
    }

    
    public InvalidMoodException(String message, Throwable cause) {
        super(message, cause);
    }
}
