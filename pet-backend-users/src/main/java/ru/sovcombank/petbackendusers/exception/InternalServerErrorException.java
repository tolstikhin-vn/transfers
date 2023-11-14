package ru.sovcombank.petbackendusers.exception;

public class InternalServerErrorException extends RuntimeException {

    public InternalServerErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
