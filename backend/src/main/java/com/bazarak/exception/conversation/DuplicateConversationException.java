package com.bazarak.exception.conversation;

public class DuplicateConversationException extends RuntimeException {
    public DuplicateConversationException(String message) {
        super(message);
    }
}
