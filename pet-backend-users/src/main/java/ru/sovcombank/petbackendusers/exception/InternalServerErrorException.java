package ru.sovcombank.petbackendusers.exception;

public class InternalServerErrorException extends RuntimeException {

    public InternalServerErrorException(Throwable cause) {
        super(cause);
    }
}
