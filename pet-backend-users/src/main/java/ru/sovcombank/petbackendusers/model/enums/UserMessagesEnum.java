package ru.sovcombank.petbackendusers.model.enums;

public enum UserMessagesEnum {
    USER_CREATED_SUCCESSFULLY_MESSAGE("Пользователь успешно создан"),
    USER_UPDATED_SUCCESSFULLY_MESSAGE("Пользователь успешно изменен"),
    USER_DELETED_SUCCESSFULLY_MESSAGE("Пользователь успешно удален"),
    USER_NOT_FOUND_MESSAGE("Не найден пользователь");

    private final String message;

    UserMessagesEnum(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}