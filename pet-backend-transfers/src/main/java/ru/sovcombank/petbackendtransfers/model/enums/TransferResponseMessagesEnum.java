package ru.sovcombank.petbackendtransfers.model.enums;

public enum TransferResponseMessagesEnum {

    TRANSFER_MAKED_SUCCESSFULLY("Перевод прошел успешно. Ваш баланс "),
    BAD_REQUEST_FOR_REQUEST_TYPE("Некорректный запрос по полю requestType"),
    BAD_REQUEST_FOR_ACCOUNT_NUMBER("Некорректный запрос по полю accountNumberTo"),
    BAD_REQUEST_FOR_PHONE_NUMBER("Некорректный запрос по полю phoneNumberTo"),
    BAD_REQUEST_FOR_CUR("Некорректный запрос по полю cur"),
    USER_NOT_FOUND("Не найден клиент по запросу"),
    ACCOUNT_NOT_FOUND("Не найден счет по запросу"),
    TRANSFER_NOT_FOUND("Не найден перевод по запросу"),
    ACCOUNT_CLOSED("Невозможно отправить/перевести деньги с заблокированного счета"),
    INSUFFICIENT_FUNDS("Недостаточно средств на счете");

    private final String message;

    TransferResponseMessagesEnum(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
