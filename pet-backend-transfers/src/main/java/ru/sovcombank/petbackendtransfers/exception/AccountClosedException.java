package ru.sovcombank.petbackendtransfers.exception;

public class AccountClosedException extends RuntimeException {

    public AccountClosedException(String message) {
        super(message);
    }
}