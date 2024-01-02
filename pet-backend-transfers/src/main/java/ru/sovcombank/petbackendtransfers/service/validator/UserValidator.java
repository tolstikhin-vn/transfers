package ru.sovcombank.petbackendtransfers.service.validator;

import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendtransfers.client.UserServiceClient;
import ru.sovcombank.petbackendtransfers.exception.UserNotFoundException;
import ru.sovcombank.petbackendtransfers.model.api.request.MakeTransferByPhoneRequest;
import ru.sovcombank.petbackendtransfers.model.api.response.GetUserResponse;
import ru.sovcombank.petbackendtransfers.model.enums.TransferResponseMessagesEnum;

@Component
public class UserValidator {

    private final UserServiceClient userServiceClient;

    public UserValidator(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    // Валидация пользователя (проверка полей isActive и isDeleted)
    public void validateUserForTransferByAccount(Integer clientId) {
        if (!userServiceClient.checkUserExistsForTransferByAccount(clientId)) {
            throw new UserNotFoundException(TransferResponseMessagesEnum.USER_NOT_FOUND.getMessage());
        }
    }

    // Валидация клиента для перевода по номеру телефона
    // (проверка полей isActive, isDeleted и совпадение номера телефона)
    public void validateUserForTransferByPhone(MakeTransferByPhoneRequest makeTransferByPhoneRequest) {
        if (!userServiceClient.checkUserExistsForTransferByPhone(
                makeTransferByPhoneRequest.getClientId(),
                makeTransferByPhoneRequest.getPhoneNumberFrom())) {
            throw new UserNotFoundException(TransferResponseMessagesEnum.USER_NOT_FOUND.getMessage());
        }
    }

    // Получение ответа с информацией о счетах
    public void validateActiveUser(GetUserResponse getUserResponse) {
        if (!getUserResponse.isActive() || getUserResponse.isDeleted()) {
            throw new UserNotFoundException(TransferResponseMessagesEnum.USER_NOT_FOUND.getMessage());
        }
    }
}
