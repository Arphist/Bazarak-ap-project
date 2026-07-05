package com.bazarak.exception.auth;

public class LoginToAccessAds extends RuntimeException{
    public LoginToAccessAds(String message){
        super(message);
    }
}
