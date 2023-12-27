package ru.sovcombank.petbackendtransfers.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.sovcombank.petbackendtransfers.builder.ResponseBuilder;
import ru.sovcombank.petbackendtransfers.builder.TransferDTOBuilder;
import ru.sovcombank.petbackendtransfers.client.AccountServiceClient;
import ru.sovcombank.petbackendtransfers.model.api.request.MakeTransferByAccountRequest;
import ru.sovcombank.petbackendtransfers.model.api.response.GetAccountResponse;
import ru.sovcombank.petbackendtransfers.model.api.response.MakeTransferResponse;
import ru.sovcombank.petbackendtransfers.model.dto.TransferDTO;
import ru.sovcombank.petbackendtransfers.model.entity.Transfer;
import ru.sovcombank.petbackendtransfers.service.helper.UpdateBalanceServiceHelper;
import ru.sovcombank.petbackendtransfers.service.validator.AccountValidator;
import ru.sovcombank.petbackendtransfers.service.validator.UserValidator;

@Service
public class TransferByAccountNumberService {

    private final AccountValidator accountValidator;

    private final UserValidator userValidator;

    private final AccountServiceClient accountServiceClient;

    private final KafkaTemplate<String, TransferDTO> kafkaTemplate;

    private final ResponseBuilder responseBuilder;

    private final UpdateBalanceServiceHelper updateBalanceServiceHelper;

    private final TransferDTOBuilder transferDTOBuilder;

    @Value("${kafka.topic.transfers-history-transaction}")
    private String kafkaTopic;

    public TransferByAccountNumberService(
            AccountValidator accountValidator,
            UserValidator userValidator,
            AccountServiceClient accountServiceClient, KafkaTemplate<String, TransferDTO> kafkaTemplate,
            ResponseBuilder responseBuilder,
            UpdateBalanceServiceHelper updateBalanceServiceHelper,
            TransferDTOBuilder transferDTOBuilder) {
        this.accountValidator = accountValidator;
        this.userValidator = userValidator;
        this.accountServiceClient = accountServiceClient;
        this.kafkaTemplate = kafkaTemplate;
        this.responseBuilder = responseBuilder;
        this.updateBalanceServiceHelper = updateBalanceServiceHelper;
        this.transferDTOBuilder = transferDTOBuilder;
    }

    /**
     * Совершает перевод по номеру счета.
     *
     * @param makeTransferByAccountRequest Запрос на перевод по номеру счета.
     * @return Ответ с сообщением о выполнении перевода.
     */
    public MakeTransferResponse makeTransferByAccount(MakeTransferByAccountRequest makeTransferByAccountRequest) {
        validateTransfer(makeTransferByAccountRequest);

        Transfer transfer = updateBalanceServiceHelper.updateBalance(
                makeTransferByAccountRequest.getCur(),
                accountServiceClient.getAccountResponse(makeTransferByAccountRequest.getAccountNumberFrom()),
                makeTransferByAccountRequest.getAccountNumberFrom(),
                makeTransferByAccountRequest.getAccountNumberTo(),
                makeTransferByAccountRequest.getAmount());

        kafkaTemplate.send(kafkaTopic, transferDTOBuilder.createTransferDTOObject(
                transfer,
                accountServiceClient.getAccountResponse(makeTransferByAccountRequest.getAccountNumberFrom()).getClientId(),
                responseBuilder.getValidateGetAccountResponse(makeTransferByAccountRequest.getAccountNumberTo()).getClientId()));

        return responseBuilder.createMakeTransferResponse(accountServiceClient.getBalanceResponse(
                makeTransferByAccountRequest.getAccountNumberFrom()).getBalance());
    }

    // Валидация данных для осуществления перевода
    private void validateTransfer(MakeTransferByAccountRequest makeTransferByAccountRequest) {
        accountValidator.checkRepeatNumbers(
                makeTransferByAccountRequest.getAccountNumberFrom(),
                makeTransferByAccountRequest.getAccountNumberTo());

        userValidator.validateUserForTransferByAccount(makeTransferByAccountRequest.getClientId());

        String accountNumberFrom = makeTransferByAccountRequest.getAccountNumberFrom();
        GetAccountResponse getAccountFromResponse = accountServiceClient.getAccountResponse(accountNumberFrom);

        accountValidator.validateAccountForTransfer(getAccountFromResponse);
        accountValidator.validateCur(makeTransferByAccountRequest.getCur(), getAccountFromResponse.getCur());

        String accountNumberTo = makeTransferByAccountRequest.getAccountNumberTo();
        GetAccountResponse getAccountToResponse = responseBuilder.getValidateGetAccountResponse(accountNumberTo);

        userValidator.validateUserForTransferByAccount(getAccountToResponse.getClientId());
    }
}
