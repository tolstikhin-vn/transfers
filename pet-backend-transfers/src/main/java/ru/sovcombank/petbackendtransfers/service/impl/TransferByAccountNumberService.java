package ru.sovcombank.petbackendtransfers.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.sovcombank.petbackendtransfers.builders.EntityBuilder;
import ru.sovcombank.petbackendtransfers.builders.RequestBuilder;
import ru.sovcombank.petbackendtransfers.builders.ResponseBuilder;
import ru.sovcombank.petbackendtransfers.client.AccountServiceClient;
import ru.sovcombank.petbackendtransfers.db.DatabaseChanger;
import ru.sovcombank.petbackendtransfers.helper.TransferHelper;
import ru.sovcombank.petbackendtransfers.model.api.request.MakeTransferByAccountRequest;
import ru.sovcombank.petbackendtransfers.model.api.request.UpdateBalanceRequest;
import ru.sovcombank.petbackendtransfers.model.api.response.GetAccountResponse;
import ru.sovcombank.petbackendtransfers.model.api.response.MakeTransferResponse;
import ru.sovcombank.petbackendtransfers.model.entity.Transfer;
import ru.sovcombank.petbackendtransfers.model.enums.TransferResponseMessagesEnum;
import ru.sovcombank.petbackendtransfers.model.enums.TypePaymentsEnum;
import ru.sovcombank.petbackendtransfers.validator.AccountValidator;
import ru.sovcombank.petbackendtransfers.validator.UserValidator;

import java.math.BigDecimal;

@Service
public class TransferByAccountNumberService {

    private final AccountValidator accountValidator;

    private final UserValidator userValidator;

    private final AccountServiceClient accountServiceClient;

    private final ResponseBuilder responseBuilder;

    private final RequestBuilder requestBuilder;

    private final KafkaTemplate<String, Transfer> kafkaTemplate;

    private final EntityBuilder entityBuilder;

    private final DatabaseChanger databaseChanger;

    private final TransferHelper transferHelper;

    @Value("${kafka.topic.transfers-history-transaction}")
    private String kafkaTopic;

    public TransferByAccountNumberService(
            AccountValidator accountValidator,
            UserValidator userValidator,
            AccountServiceClient accountServiceClient,
            ResponseBuilder responseBuilder,
            RequestBuilder requestBuilder,
            KafkaTemplate<String, Transfer> kafkaTemplate,
            EntityBuilder entityBuilder,
            DatabaseChanger databaseChanger,
            TransferHelper transferHelper) {
        this.accountValidator = accountValidator;
        this.userValidator = userValidator;
        this.accountServiceClient = accountServiceClient;
        this.responseBuilder = responseBuilder;
        this.requestBuilder = requestBuilder;
        this.kafkaTemplate = kafkaTemplate;
        this.entityBuilder = entityBuilder;
        this.databaseChanger = databaseChanger;
        this.transferHelper = transferHelper;
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

        String clientIdFrom = makeTransferByAccountRequest.getClientId();
        userValidator.validateUserForTransferByAccount(clientIdFrom);

        String accountNumberFrom = makeTransferByAccountRequest.getAccountNumberFrom();

        GetAccountResponse getAccountFromResponse = accountServiceClient.getAccountResponse(accountNumberFrom);

        accountValidator.validateAccountForTransfer(getAccountFromResponse);
        accountValidator.validateCur(makeTransferByAccountRequest.getCur(), getAccountFromResponse.getCur());

        BigDecimal transferAmount = makeTransferByAccountRequest.getAmount();
        accountValidator.validateSufficientFunds(accountNumberFrom, transferAmount);

        String accountNumberTo = makeTransferByAccountRequest.getAccountNumberTo();
        GetAccountResponse getAccountToResponse = responseBuilder.getValidateGetAccountResponse(accountNumberTo);

        String clientIdTo = getAccountToResponse.getClientId();

        BigDecimal amountByCur = transferHelper.getAmountByCur(
                makeTransferByAccountRequest.getCur(),
                transferAmount,
                getAccountToResponse
        );

        userValidator.validateUserForTransferByAccount(clientIdTo);

        UpdateBalanceRequest updateBalanceRequestForAccountFrom = requestBuilder.createUpdateBalanceRequest(
                TypePaymentsEnum.DEBITING.getTypePayment(),
                transferAmount
        );

        UpdateBalanceRequest updateBalanceRequestForAccountTo = requestBuilder.createUpdateBalanceRequest(
                TypePaymentsEnum.REPLENISHMENT.getTypePayment(),
                amountByCur
        );

        databaseChanger.updateAccountBalance(makeTransferByAccountRequest.getAccountNumberFrom(), updateBalanceRequestForAccountFrom);
        databaseChanger.updateAccountBalance(makeTransferByAccountRequest.getAccountNumberTo(), updateBalanceRequestForAccountTo);

        Transfer transfer = entityBuilder.createTransferObject(
                makeTransferByAccountRequest.getAccountNumberFrom(),
                makeTransferByAccountRequest.getAccountNumberTo(),
                transferAmount,
                makeTransferByAccountRequest.getCur()
        );

        databaseChanger.saveTransfer(transfer);

        kafkaTemplate.send(kafkaTopic, transfer);

        return responseBuilder.createMakeTransferResponse(accountServiceClient.getBalanceResponse(accountNumberFrom).getBalance());
    }
}
