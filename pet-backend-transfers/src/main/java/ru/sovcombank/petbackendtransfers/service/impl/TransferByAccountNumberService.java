package ru.sovcombank.petbackendtransfers.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import ru.sovcombank.petbackendtransfers.builder.ResponseBuilder;
import ru.sovcombank.petbackendtransfers.builder.TransferDTOBuilder;
import ru.sovcombank.petbackendtransfers.client.AccountServiceClient;
import ru.sovcombank.petbackendtransfers.mapping.impl.MapToMakeTransferByAccountRequest;
import ru.sovcombank.petbackendtransfers.model.api.request.MakeTransferByAccountRequest;
import ru.sovcombank.petbackendtransfers.model.api.response.GetAccountResponse;
import ru.sovcombank.petbackendtransfers.model.api.response.MakeTransferResponse;
import ru.sovcombank.petbackendtransfers.model.dto.TransferDTO;
import ru.sovcombank.petbackendtransfers.model.entity.Transfer;
import ru.sovcombank.petbackendtransfers.service.TransferStrategy;
import ru.sovcombank.petbackendtransfers.service.helper.UpdateBalanceServiceHelper;
import ru.sovcombank.petbackendtransfers.service.validator.AccountValidator;
import ru.sovcombank.petbackendtransfers.service.validator.UserValidator;

import java.util.Map;

@Slf4j
@Service
public class TransferByAccountNumberService implements TransferStrategy {

    private final AccountValidator accountValidator;

    private final UserValidator userValidator;

    private final AccountServiceClient accountServiceClient;

    private final KafkaTemplate<String, TransferDTO> kafkaTemplate;

    private final ResponseBuilder responseBuilder;

    private final UpdateBalanceServiceHelper updateBalanceServiceHelper;

    private final TransferDTOBuilder transferDTOBuilder;

    private final MapToMakeTransferByAccountRequest mapToMakeTransferByAccountRequest;

    @Value("${kafka.topic.transfers-history-transaction}")
    private String kafkaTopic;

    public TransferByAccountNumberService(
            AccountValidator accountValidator,
            UserValidator userValidator,
            AccountServiceClient accountServiceClient,
            KafkaTemplate<String, TransferDTO> kafkaTemplate,
            ResponseBuilder responseBuilder,
            UpdateBalanceServiceHelper updateBalanceServiceHelper,
            TransferDTOBuilder transferDTOBuilder,
            MapToMakeTransferByAccountRequest mapToMakeTransferByAccountRequest) {
        this.accountValidator = accountValidator;
        this.userValidator = userValidator;
        this.accountServiceClient = accountServiceClient;
        this.kafkaTemplate = kafkaTemplate;
        this.responseBuilder = responseBuilder;
        this.updateBalanceServiceHelper = updateBalanceServiceHelper;
        this.transferDTOBuilder = transferDTOBuilder;
        this.mapToMakeTransferByAccountRequest = mapToMakeTransferByAccountRequest;
    }

    /**
     * Совершает перевод по номеру счета.
     *
     * @param requestMap Запрос на перевод по номеру счета.
     * @return Ответ с сообщением о выполнении перевода.
     */
    public MakeTransferResponse makeTransfer(Map<String, Object> requestMap) {
        MakeTransferByAccountRequest makeTransferByAccountRequest =
                mapToMakeTransferByAccountRequest.map(requestMap);

        validateTransfer(makeTransferByAccountRequest);

        Transfer transfer = updateBalanceServiceHelper.updateBalance(
                makeTransferByAccountRequest.getCur(),
                accountServiceClient.getAccountResponse(makeTransferByAccountRequest.getAccountNumberTo()),
                makeTransferByAccountRequest.getAccountNumberFrom(),
                makeTransferByAccountRequest.getAccountNumberTo(),
                makeTransferByAccountRequest.getAmount());

        sendKafkaMessage(transfer, makeTransferByAccountRequest);

        log.info("The transfer from account {} to account{} in the amount of {} was completed successfully",
                transfer.getAccountNumberFrom(), transfer.getAccountNumberTo(), transfer.getAmount());

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
        accountValidator.validateSufficientFunds(accountNumberFrom, makeTransferByAccountRequest.getAmount());

        String accountNumberTo = makeTransferByAccountRequest.getAccountNumberTo();
        GetAccountResponse getAccountToResponse = responseBuilder.getValidateGetAccountResponse(accountNumberTo);

        userValidator.validateUserForTransferByAccount(getAccountToResponse.getClientId());
    }

    @Retryable(retryFor = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    private void sendKafkaMessage(Transfer transfer, MakeTransferByAccountRequest makeTransferByAccountRequest) {
        kafkaTemplate.send(kafkaTopic, transferDTOBuilder.createTransferDTOObject(
                transfer,
                accountServiceClient.getAccountResponse(makeTransferByAccountRequest.getAccountNumberFrom()).getClientId(),
                responseBuilder.getValidateGetAccountResponse(makeTransferByAccountRequest.getAccountNumberTo()).getClientId()));
    }
}
