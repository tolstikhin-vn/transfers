package ru.sovcombank.petbackendtransfers.builder;

import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendtransfers.client.AccountServiceClient;
import ru.sovcombank.petbackendtransfers.client.UserServiceClient;
import ru.sovcombank.petbackendtransfers.exception.UserNotFoundException;
import ru.sovcombank.petbackendtransfers.model.api.response.GetAccountResponse;
import ru.sovcombank.petbackendtransfers.model.api.response.GetAccountsResponse;
import ru.sovcombank.petbackendtransfers.model.api.response.MakeTransferResponse;
import ru.sovcombank.petbackendtransfers.model.enums.TransferResponseMessagesEnum;

import java.math.BigDecimal;

@Component
public class ResponseBuilder {

    private final AccountServiceClient accountServiceClient;

    private final UserServiceClient userServiceClient;

    public ResponseBuilder(AccountServiceClient accountServiceClient, UserServiceClient userServiceClient) {
        this.accountServiceClient = accountServiceClient;
        this.userServiceClient = userServiceClient;
    }

    // Получение валидного ответа с информацией о счете
    public GetAccountResponse getValidateGetAccountResponse(String accountNumberTo) {
        GetAccountResponse getAccountToResponse = accountServiceClient.getAccountResponse(accountNumberTo);
        if (!userServiceClient.checkUserExistsForTransferByAccount(getAccountToResponse.getClientId())) {
            throw new UserNotFoundException(TransferResponseMessagesEnum.USER_NOT_FOUND.getMessage());
        }
        return getAccountToResponse;
    }

    // Получение ответа с информацией о счете
    public GetAccountResponse getAccountResponse(String accountNumber) {
        return accountServiceClient.getAccountResponse(accountNumber);
    }

    // Получение ответа с информацией о счетах
    public GetAccountsResponse getAccountsResponse(String clientId) {
        return accountServiceClient.getAccountsResponse(clientId);
    }

    // Получение ответа с информацией о совершенном переводе
    public MakeTransferResponse createMakeTransferResponse(BigDecimal balance) {
        return new MakeTransferResponse(
                TransferResponseMessagesEnum.TRANSFER_MAKED_SUCCESSFULLY.getMessage() + balance
        );
    }
}
