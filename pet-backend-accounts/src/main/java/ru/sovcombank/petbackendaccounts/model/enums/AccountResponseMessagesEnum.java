package ru.sovcombank.petbackendaccounts.model.enums;

public enum AccountResponseMessagesEnum {

    ACCOUNT_CREATED_SUCCESSFULLY("Счет успешно создан"),
    BAD_REQUEST_FOR_CUR("Некорректный запрос по полю cur"),
    BAD_REQUEST_FOR_TYPE_PAY("Некорректный запрос по полю typePayments"),
    BAD_REQUEST_FOR_AMOUNT("Некорректный запрос по полю amount"),
    USER_NOT_FOUND("Не найден клиент по запросу"),
    ACCOUNT_NOT_FOUND("Не найден счет по запросу"),
    ACCOUNT_DELETED_SUCCESSFULLY("Счет успешно закрыт"),
    BALANCE_UPDATED_SUCCESSFULLY("Баланс успешно обновлен. Ваш баланс: ");

    private final String message;

    AccountResponseMessagesEnum(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
