package com.bazarak.exception.conversation;

public class SelfMessagingException extends RuntimeException{
    public SelfMessagingException(String message){
        super(message);
    }
}
