package ru.sovcombank.petbackendhistory.model.enums;

public enum HistoryResponseMessagesEnum {

    USER_NOT_FOUND("Не найден клиент по запросу");

    private final String message;

    HistoryResponseMessagesEnum(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
