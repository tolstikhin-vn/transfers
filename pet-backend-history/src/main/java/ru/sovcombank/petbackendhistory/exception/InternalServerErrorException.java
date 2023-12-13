package ru.sovcombank.petbackendhistory.exception;

public class InternalServerErrorException extends RuntimeException {

    public InternalServerErrorException(Throwable cause) {
        super(cause);
    }
}
