package com.chat.exception;

public class UnableToGetAccessTokenException extends RuntimeException {

    public UnableToGetAccessTokenException(String message) {
        super(message);
    }
}
