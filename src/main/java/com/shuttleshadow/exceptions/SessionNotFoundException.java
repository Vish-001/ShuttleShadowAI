package com.shuttleshadow.exceptions;

public class SessionNotFoundException extends RuntimeException {
    public SessionNotFoundException(Long sessionId) {
        super("Session not found with ID: " + sessionId);
    }
}