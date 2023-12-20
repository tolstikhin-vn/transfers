package ru.sovcombank.petbackendtransfers.validator;

import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendtransfers.client.UserServiceClient;
import ru.sovcombank.petbackendtransfers.exception.BadRequestException;
import ru.sovcombank.petbackendtransfers.exception.UserNotFoundException;
import ru.sovcombank.petbackendtransfers.model.api.response.GetUserResponse;
import ru.sovcombank.petbackendtransfers.model.enums.TransferResponseMessagesEnum;

@Component
public class UserValidator {

    private final UserServiceClient userServiceClient;

    public UserValidator(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    // Валидация пользователя (проверка полей isActive и isDeleted)
    public void validateUserForTransferByAccount(String clientId) {
        if (!userServiceClient.checkUserExistsForTransferByAccount(clientId)) {
            throw new UserNotFoundException(TransferResponseMessagesEnum.USER_NOT_FOUND.getMessage());
        }
    }

    // Валидация клиента для перевода по номеру телефона
    // (проверка полей isActive, isDeleted и совпадение номера телефона)
    public void validateUserForTransferByPhone(String clientIdFrom, String phoneNumberFrom) {
        if (!userServiceClient.checkUserExistsForTransferByPhone(clientIdFrom, phoneNumberFrom)) {
            throw new UserNotFoundException(TransferResponseMessagesEnum.USER_NOT_FOUND.getMessage());
        }
    }

    // Получение ответа с информацией о счетах
    public void validateActiveUser(GetUserResponse getUserResponse) {
        if (!getUserResponse.isActive() || getUserResponse.isDeleted()) {
            throw new UserNotFoundException(TransferResponseMessagesEnum.USER_NOT_FOUND.getMessage());
        }
    }

    // Проверка на перевод самому себе
    public void checkRepeatNumbers(String numberFrom, String numberTo, String message) {
        if (numberTo.equals(numberFrom)) {
            throw new BadRequestException(message);
        }
    }
}
