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
import ru.sovcombank.petbackendtransfers.client.UserServiceClient;
import ru.sovcombank.petbackendtransfers.mapping.impl.MapToMakeTransferByPhoneRequest;
import ru.sovcombank.petbackendtransfers.model.api.request.MakeTransferByPhoneRequest;
import ru.sovcombank.petbackendtransfers.model.api.response.GetAccountResponse;
import ru.sovcombank.petbackendtransfers.model.api.response.GetUserResponse;
import ru.sovcombank.petbackendtransfers.model.api.response.MakeTransferResponse;
import ru.sovcombank.petbackendtransfers.model.dto.TransferDTO;
import ru.sovcombank.petbackendtransfers.model.entity.Transfer;
import ru.sovcombank.petbackendtransfers.service.TransferStrategy;
import ru.sovcombank.petbackendtransfers.service.helper.GetMainAccountServiceHelper;
import ru.sovcombank.petbackendtransfers.service.helper.UpdateBalanceServiceHelper;
import ru.sovcombank.petbackendtransfers.service.validator.AccountValidator;
import ru.sovcombank.petbackendtransfers.service.validator.UserValidator;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Service
public class TransferByPhoneNumberService implements TransferStrategy {

    private final AccountValidator accountValidator;

    private final UserValidator userValidator;

    private final AccountServiceClient accountServiceClient;

    private final UserServiceClient userServiceClient;

    private final ResponseBuilder responseBuilder;

    private final GetMainAccountServiceHelper getMainAccountServiceHelper;

    private final UpdateBalanceServiceHelper updateBalanceServiceHelper;

    private final TransferDTOBuilder transferDTOBuilder;

    private final MapToMakeTransferByPhoneRequest mapToMakeTransferByPhoneRequest;

    private final KafkaTemplate<String, TransferDTO> kafkaTemplate;

    @Value("${kafka.topic.transfers-history-transaction}")
    private String kafkaTopic;

    public TransferByPhoneNumberService(
            AccountValidator accountValidator,
            UserValidator userValidator,
            AccountServiceClient accountServiceClient,
            UserServiceClient userServiceClient,
            ResponseBuilder responseBuilder,
            GetMainAccountServiceHelper getMainAccountServiceHelper,
            UpdateBalanceServiceHelper updateBalanceServiceHelper,
            TransferDTOBuilder transferDTOBuilder,
            MapToMakeTransferByPhoneRequest mapToMakeTransferByPhoneRequest,
            KafkaTemplate<String, TransferDTO> kafkaTemplate) {
        this.accountValidator = accountValidator;
        this.userValidator = userValidator;
        this.accountServiceClient = accountServiceClient;
        this.userServiceClient = userServiceClient;
        this.responseBuilder = responseBuilder;
        this.getMainAccountServiceHelper = getMainAccountServiceHelper;
        this.updateBalanceServiceHelper = updateBalanceServiceHelper;
        this.transferDTOBuilder = transferDTOBuilder;
        this.mapToMakeTransferByPhoneRequest = mapToMakeTransferByPhoneRequest;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Совершает перевод по номеру телефона.
     *
     * @param requestMap Запрос на перевод по номеру телефона.
     * @return Ответ с сообщением о выполнении перевода.
     */
    public MakeTransferResponse makeTransfer(Map<String, Object> requestMap) {
        MakeTransferByPhoneRequest makeTransferByPhoneRequest =
                mapToMakeTransferByPhoneRequest.map(requestMap);

        validateTransfer(makeTransferByPhoneRequest);

        Transfer transfer = updateBalanceServiceHelper.updateBalance(
                makeTransferByPhoneRequest.getCur(),
                responseBuilder.getAccountResponse(getMainAccountServiceHelper.getMainAccount(responseBuilder
                        .getAccountsResponse(getClientId(makeTransferByPhoneRequest))
                        .getAccountList())),
                getMainAccountFrom(makeTransferByPhoneRequest),
                getMainAccountTo(makeTransferByPhoneRequest),
                makeTransferByPhoneRequest.getAmount());

        sendKafkaMessage(transfer, makeTransferByPhoneRequest);

        log.info("The transfer from account {} to account{} in the amount of {} was completed successfully",
                transfer.getAccountNumberFrom(), transfer.getAccountNumberTo(), transfer.getAmount());

        return responseBuilder.createMakeTransferResponse(accountServiceClient.getBalanceResponse(
                getMainAccountFrom(makeTransferByPhoneRequest)).getBalance());
    }

    // Валидация данных для осуществления перевода
    private void validateTransfer(MakeTransferByPhoneRequest makeTransferByPhoneRequest) {
        accountValidator.checkRepeatNumbers(
                makeTransferByPhoneRequest.getPhoneNumberFrom(),
                makeTransferByPhoneRequest.getPhoneNumberTo());

        userValidator.validateUserForTransferByPhone(makeTransferByPhoneRequest);

        String mainAccountFrom = getMainAccountServiceHelper.getMainAccount(responseBuilder.getAccountsResponse(
                makeTransferByPhoneRequest.getClientId()).getAccountList());
        GetAccountResponse getAccountFromResponse = responseBuilder.getAccountResponse(mainAccountFrom);

        accountValidator.validateAccountForTransfer(getAccountFromResponse);

        BigDecimal transferAmount = makeTransferByPhoneRequest.getAmount();
        String cur = makeTransferByPhoneRequest.getCur();
        accountValidator.validateCur(cur, getAccountFromResponse.getCur());

        accountValidator.validateSufficientFunds(mainAccountFrom, transferAmount);

        GetUserResponse getUserResponse = userServiceClient.getUserInfo(makeTransferByPhoneRequest.getPhoneNumberTo());
        userValidator.validateActiveUser(getUserResponse);
    }

    private String getMainAccountFrom(MakeTransferByPhoneRequest makeTransferByPhoneRequest) {
        return getMainAccountServiceHelper.getMainAccount(responseBuilder.getAccountsResponse(
                makeTransferByPhoneRequest.getClientId()).getAccountList());
    }

    private String getMainAccountTo(MakeTransferByPhoneRequest makeTransferByPhoneRequest) {
        return getMainAccountServiceHelper.getMainAccount(responseBuilder.getAccountsResponse(
                        userServiceClient.getUserInfo(makeTransferByPhoneRequest.getPhoneNumberTo()).getId())
                .getAccountList());
    }

    private Integer getClientId(MakeTransferByPhoneRequest makeTransferByPhoneRequest) {
        return userServiceClient.getUserInfo(makeTransferByPhoneRequest.getPhoneNumberTo()).getId();
    }

    @Retryable(retryFor = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    private void sendKafkaMessage(Transfer transfer, MakeTransferByPhoneRequest makeTransferByPhoneRequest) {
        kafkaTemplate.send(kafkaTopic, transferDTOBuilder.createTransferDTOObject(transfer,
                accountServiceClient.getAccountResponse(getMainAccountFrom(makeTransferByPhoneRequest)).getClientId(),
                responseBuilder.getValidateGetAccountResponse(getMainAccountTo(makeTransferByPhoneRequest)).getClientId()));
    }
}