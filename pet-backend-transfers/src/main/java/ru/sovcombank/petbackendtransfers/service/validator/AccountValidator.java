package ru.sovcombank.petbackendtransfers.service.validator;

import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendtransfers.client.AccountServiceClient;
import ru.sovcombank.petbackendtransfers.exception.AccountClosedException;
import ru.sovcombank.petbackendtransfers.exception.BadRequestException;
import ru.sovcombank.petbackendtransfers.exception.InsufficientFundsException;
import ru.sovcombank.petbackendtransfers.model.api.response.GetAccountResponse;
import ru.sovcombank.petbackendtransfers.model.enums.TransferResponseMessagesEnum;

import java.math.BigDecimal;

@Component
public class AccountValidator {

    private final AccountServiceClient accountServiceClient;

    public AccountValidator(AccountServiceClient accountServiceClient) {
        this.accountServiceClient = accountServiceClient;
    }

    // Валидация счета (проверка поля isClosed)
    public void validateAccountForTransfer(GetAccountResponse getAccountFromResponse) {
        if (getAccountFromResponse.isClosed()) {
            throw new AccountClosedException(TransferResponseMessagesEnum.ACCOUNT_CLOSED.getMessage());
        }
    }

    // Валидация суммы перевода (должна быть не больше, чем сумма на балансе)
    public void validateSufficientFunds(String accountNumber, BigDecimal transferAmount) {
        GetAccountResponse getAccountFromResponse = accountServiceClient.getAccountResponse(accountNumber);
        if (transferAmount.compareTo(getAccountFromResponse.getBalance()) > 0) {
            throw new InsufficientFundsException(TransferResponseMessagesEnum.INSUFFICIENT_FUNDS.getMessage());
        }
    }

    // Валидация валюты (из запроса должна совпадать с фактической)
    public void validateCur(String curFromRequest, String realCur) {
        if (!curFromRequest.equals(realCur)) {
            throw new BadRequestException(TransferResponseMessagesEnum.BAD_REQUEST_FOR_CUR.getMessage());
        }
    }

    // Проверка на перевод самому себе
    public void checkRepeatNumbers(String numberFrom, String numberTo) {
        if (numberTo.equals(numberFrom)) {
            throw new BadRequestException(TransferResponseMessagesEnum.BAD_REQUEST_FOR_ACCOUNT_NUMBER.getMessage());
        }
    }
}
