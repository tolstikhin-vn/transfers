package ru.sovcombank.petbackendtransfers.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ru.sovcombank.petbackendtransfers.builder.ResponseBuilder;
import ru.sovcombank.petbackendtransfers.client.AccountServiceClient;
import ru.sovcombank.petbackendtransfers.helper.TransferServiceHelper;
import ru.sovcombank.petbackendtransfers.model.api.request.MakeTransferByAccountRequest;
import ru.sovcombank.petbackendtransfers.model.api.response.GetAccountResponse;
import ru.sovcombank.petbackendtransfers.model.api.response.MakeTransferResponse;
import ru.sovcombank.petbackendtransfers.model.enums.TransferResponseMessagesEnum;
import ru.sovcombank.petbackendtransfers.validator.AccountValidator;
import ru.sovcombank.petbackendtransfers.validator.UserValidator;

import java.math.BigDecimal;

@Service
public class TransferByAccountNumberService {

    private final AccountValidator accountValidator;

    private final UserValidator userValidator;

    private final AccountServiceClient accountServiceClient;

    private final ResponseBuilder responseBuilder;

    private final TransferServiceHelper transferServiceHelper;

    public TransferByAccountNumberService(
            AccountValidator accountValidator,
            UserValidator userValidator,
            AccountServiceClient accountServiceClient,
            ResponseBuilder responseBuilder,
            TransferServiceHelper transferServiceHelper) {
        this.accountValidator = accountValidator;
        this.userValidator = userValidator;
        this.accountServiceClient = accountServiceClient;
        this.responseBuilder = responseBuilder;
        this.transferServiceHelper = transferServiceHelper;
    }

    /**
     * Совершает перевод по номеру счета.
     *
     * @param makeTransferByAccountRequest Запрос на перевод по номеру счета.
     * @return Ответ с сообщением о выполнении перевода.
     */
    @Transactional
    public MakeTransferResponse makeTransferByAccount(MakeTransferByAccountRequest makeTransferByAccountRequest) {
        accountValidator.checkRepeatNumbers(
                makeTransferByAccountRequest.getAccountNumberFrom(),
                makeTransferByAccountRequest.getAccountNumberTo(),
                TransferResponseMessagesEnum.BAD_REQUEST_FOR_ACCOUNT_NUMBER.getMessage());

        userValidator.validateUserForTransferByAccount(makeTransferByAccountRequest.getClientId());

        String accountNumberFrom = makeTransferByAccountRequest.getAccountNumberFrom();

        GetAccountResponse getAccountFromResponse = accountServiceClient.getAccountResponse(accountNumberFrom);

        String cur = makeTransferByAccountRequest.getCur();
        accountValidator.validateAccountForTransfer(getAccountFromResponse);
        accountValidator.validateCur(cur, getAccountFromResponse.getCur());

        BigDecimal transferAmount = makeTransferByAccountRequest.getAmount();
        accountValidator.validateSufficientFunds(accountNumberFrom, transferAmount);

        String accountNumberTo = makeTransferByAccountRequest.getAccountNumberTo();
        GetAccountResponse getAccountResponseTo = responseBuilder.getValidateGetAccountResponse(accountNumberTo);

        userValidator.validateUserForTransferByAccount(getAccountResponseTo.getClientId());

        transferServiceHelper.updateBalance(cur, getAccountResponseTo, accountNumberFrom, accountNumberTo, transferAmount);

        transferServiceHelper.saveAndSendTransfer(accountNumberFrom, accountNumberTo, transferAmount, cur);

        return responseBuilder.createMakeTransferResponse(accountServiceClient.getBalanceResponse(accountNumberFrom).getBalance());
    }
}
