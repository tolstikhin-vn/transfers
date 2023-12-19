package ru.sovcombank.petbackendtransfers.exception;

public class InternalServerErrorException extends RuntimeException {

    public InternalServerErrorException(Throwable cause) {
        super(cause);
    }
}