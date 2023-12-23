package ru.sovcombank.petbackendtransfers.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ru.sovcombank.petbackendtransfers.builder.ResponseBuilder;
import ru.sovcombank.petbackendtransfers.client.AccountServiceClient;
import ru.sovcombank.petbackendtransfers.client.UserServiceClient;
import ru.sovcombank.petbackendtransfers.helper.TransferServiceHelper;
import ru.sovcombank.petbackendtransfers.model.api.request.MakeTransferByPhoneRequest;
import ru.sovcombank.petbackendtransfers.model.api.response.GetAccountResponse;
import ru.sovcombank.petbackendtransfers.model.api.response.GetAccountsResponse;
import ru.sovcombank.petbackendtransfers.model.api.response.GetUserResponse;
import ru.sovcombank.petbackendtransfers.model.api.response.MakeTransferResponse;
import ru.sovcombank.petbackendtransfers.model.enums.TransferResponseMessagesEnum;
import ru.sovcombank.petbackendtransfers.validator.AccountValidator;
import ru.sovcombank.petbackendtransfers.validator.UserValidator;

import java.math.BigDecimal;

@Service
public class TransferByPhoneNumberService {

    private final AccountValidator accountValidator;

    private final UserValidator userValidator;

    private final AccountServiceClient accountServiceClient;

    private final UserServiceClient userServiceClient;

    private final ResponseBuilder responseBuilder;

    private final TransferServiceHelper transferServiceHelper;

    public TransferByPhoneNumberService(
            AccountValidator accountValidator,
            UserValidator userValidator,
            AccountServiceClient accountServiceClient,
            UserServiceClient userServiceClient,
            ResponseBuilder responseBuilder,
            TransferServiceHelper transferServiceHelper) {
        this.accountValidator = accountValidator;
        this.userValidator = userValidator;
        this.accountServiceClient = accountServiceClient;
        this.userServiceClient = userServiceClient;
        this.responseBuilder = responseBuilder;
        this.transferServiceHelper = transferServiceHelper;
    }

    /**
     * Совершает перевод по номеру телефона.
     *
     * @param makeTransferByPhoneRequest Запрос на перевод по номеру телефона.
     * @return Ответ с сообщением о выполнении перевода.
     */
    @Transactional
    public MakeTransferResponse makeTransferByPhone(MakeTransferByPhoneRequest makeTransferByPhoneRequest) {
        accountValidator.checkRepeatNumbers(
                makeTransferByPhoneRequest.getPhoneNumberFrom(),
                makeTransferByPhoneRequest.getPhoneNumberTo(),
                TransferResponseMessagesEnum.BAD_REQUEST_FOR_ACCOUNT_NUMBER.getMessage());

        userValidator.validateUserForTransferByPhone(makeTransferByPhoneRequest);

        String mainAccountFrom = transferServiceHelper.getMainAccount(responseBuilder.getAccountsResponse(
                makeTransferByPhoneRequest.getClientId()).getAccountList());
        GetAccountResponse getAccountResponseFrom = responseBuilder.getAccountResponse(mainAccountFrom);

        accountValidator.validateAccountForTransfer(getAccountResponseFrom);

        BigDecimal transferAmount = makeTransferByPhoneRequest.getAmount();
        String cur = makeTransferByPhoneRequest.getCur();
        accountValidator.validateCur(cur, getAccountResponseFrom.getCur());

        BigDecimal amountByCur = transferServiceHelper.getAmountByCur(cur, transferAmount, getAccountResponseFrom);
        accountValidator.validateSufficientFunds(mainAccountFrom, amountByCur);

        GetUserResponse getUserResponse = userServiceClient.getUserInfo(makeTransferByPhoneRequest.getPhoneNumberTo());
        userValidator.validateActiveUser(getUserResponse);

        GetAccountsResponse getAccountsResponseTo = responseBuilder.getAccountsResponse(Integer.toString(getUserResponse.getId()));
        String mainAccountTo = transferServiceHelper.getMainAccount(getAccountsResponseTo.getAccountList());

        transferServiceHelper.updateBalance(cur, getAccountResponseFrom, mainAccountFrom, mainAccountTo, transferAmount);

        transferServiceHelper.saveAndSendTransfer(mainAccountFrom, mainAccountTo, transferAmount, cur);

        return responseBuilder.createMakeTransferResponse(accountServiceClient.getBalanceResponse(mainAccountFrom).getBalance());
    }
}