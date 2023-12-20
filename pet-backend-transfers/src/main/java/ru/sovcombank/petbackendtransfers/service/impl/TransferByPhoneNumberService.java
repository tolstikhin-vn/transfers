package ru.sovcombank.petbackendtransfers.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.sovcombank.petbackendtransfers.builders.EntityBuilder;
import ru.sovcombank.petbackendtransfers.builders.RequestBuilder;
import ru.sovcombank.petbackendtransfers.builders.ResponseBuilder;
import ru.sovcombank.petbackendtransfers.client.AccountServiceClient;
import ru.sovcombank.petbackendtransfers.client.UserServiceClient;
import ru.sovcombank.petbackendtransfers.db.DatabaseChanger;
import ru.sovcombank.petbackendtransfers.helper.TransferHelper;
import ru.sovcombank.petbackendtransfers.model.api.request.MakeTransferByPhoneRequest;
import ru.sovcombank.petbackendtransfers.model.api.request.UpdateBalanceRequest;
import ru.sovcombank.petbackendtransfers.model.api.response.GetAccountResponse;
import ru.sovcombank.petbackendtransfers.model.api.response.GetAccountsResponse;
import ru.sovcombank.petbackendtransfers.model.api.response.GetUserResponse;
import ru.sovcombank.petbackendtransfers.model.api.response.MakeTransferResponse;
import ru.sovcombank.petbackendtransfers.model.entity.Transfer;
import ru.sovcombank.petbackendtransfers.model.enums.TransferResponseMessagesEnum;
import ru.sovcombank.petbackendtransfers.model.enums.TypePaymentsEnum;
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

    private final RequestBuilder requestBuilder;

    private final KafkaTemplate<String, Transfer> kafkaTemplate;

    private final EntityBuilder entityBuilder;

    private final DatabaseChanger databaseChanger;

    private final TransferHelper transferHelper;

    @Value("${kafka.topic.transfers-history-transaction}")
    private String kafkaTopic;

    public TransferByPhoneNumberService(
            AccountValidator accountValidator,
            UserValidator userValidator,
            AccountServiceClient accountServiceClient,
            UserServiceClient userServiceClient,
            ResponseBuilder responseBuilder,
            RequestBuilder requestBuilder,
            KafkaTemplate<String, Transfer> kafkaTemplate,
            EntityBuilder entityBuilder,
            DatabaseChanger databaseChanger,
            TransferHelper transferHelper) {
        this.accountValidator = accountValidator;
        this.userValidator = userValidator;
        this.accountServiceClient = accountServiceClient;
        this.userServiceClient = userServiceClient;
        this.responseBuilder = responseBuilder;
        this.requestBuilder = requestBuilder;
        this.kafkaTemplate = kafkaTemplate;
        this.entityBuilder = entityBuilder;
        this.databaseChanger = databaseChanger;
        this.transferHelper = transferHelper;
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

        String clientIdFrom = makeTransferByPhoneRequest.getClientId();
        String phoneNumberFrom = makeTransferByPhoneRequest.getPhoneNumberFrom();

        userValidator.validateUserForTransferByPhone(clientIdFrom, phoneNumberFrom);
        System.out.println("все хорошо");

        GetAccountsResponse getAccountsResponseFrom = responseBuilder.getAccountsResponse(clientIdFrom);
        String mainAccountFrom = transferHelper.getMainAccount(getAccountsResponseFrom.getAccountList());
        System.out.println("вошел");
        GetAccountResponse getAccountResponseFrom = responseBuilder.getAccountResponse(mainAccountFrom);
        System.out.println("вышел");
        accountValidator.validateAccountForTransfer(getAccountResponseFrom);

        accountValidator.validateCur(makeTransferByPhoneRequest.getCur(), getAccountResponseFrom.getCur());

        BigDecimal amountByCur = transferHelper.getAmountByCur(
                makeTransferByPhoneRequest.getCur(),
                makeTransferByPhoneRequest.getAmount(),
                getAccountResponseFrom
        );

        accountValidator.validateSufficientFunds(mainAccountFrom, amountByCur);

        String phoneNumberTo = makeTransferByPhoneRequest.getPhoneNumberTo();
        GetUserResponse getUserResponse = userServiceClient.getUserInfo(phoneNumberTo);
        userValidator.validateActiveUser(getUserResponse);
        System.out.println("я тут");
        GetAccountsResponse getAccountsResponseTo = responseBuilder.getAccountsResponse(Integer.toString(getUserResponse.getId()));
        String mainAccountTo = transferHelper.getMainAccount(getAccountsResponseTo.getAccountList());

        BigDecimal transferAmount = makeTransferByPhoneRequest.getAmount();

        UpdateBalanceRequest updateBalanceRequestForAccountFrom = requestBuilder.createUpdateBalanceRequest(
                TypePaymentsEnum.DEBITING.getTypePayment(),
                transferAmount
        );

        UpdateBalanceRequest updateBalanceRequestForAccountTo = requestBuilder.createUpdateBalanceRequest(
                TypePaymentsEnum.REPLENISHMENT.getTypePayment(),
                amountByCur
        );

        databaseChanger.updateAccountBalance(mainAccountFrom, updateBalanceRequestForAccountFrom);
        databaseChanger.updateAccountBalance(mainAccountTo, updateBalanceRequestForAccountTo);

        Transfer transfer = entityBuilder.createTransferObject(
                mainAccountFrom,
                mainAccountTo,
                transferAmount,
                makeTransferByPhoneRequest.getCur()
        );

        databaseChanger.saveTransfer(transfer);

        kafkaTemplate.send(kafkaTopic, transfer);

        return responseBuilder.createMakeTransferResponse(accountServiceClient.getBalanceResponse(mainAccountFrom).getBalance());
    }
}
