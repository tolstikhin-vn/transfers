package ru.sovcombank.petbackendaccounts.exception;

public class InternalServerErrorException extends RuntimeException {

    public InternalServerErrorException(Throwable cause) {
        super(cause);
    }
}