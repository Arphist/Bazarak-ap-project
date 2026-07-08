package com.bazarak.exception.conversation;

public class ConversationNotFoundException extends RuntimeException {
    public ConversationNotFoundException(String message) {
        super(message);
    }
}
