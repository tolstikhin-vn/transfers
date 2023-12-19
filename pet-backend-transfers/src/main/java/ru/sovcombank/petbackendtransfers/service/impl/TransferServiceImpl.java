package ru.sovcombank.petbackendtransfers.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.sovcombank.petbackendtransfers.client.AccountServiceClient;
import ru.sovcombank.petbackendtransfers.client.UserServiceClient;
import ru.sovcombank.petbackendtransfers.converter.CurrencyConverter;
import ru.sovcombank.petbackendtransfers.exception.AccountClosedException;
import ru.sovcombank.petbackendtransfers.exception.AccountNotFoundException;
import ru.sovcombank.petbackendtransfers.exception.BadRequestException;
import ru.sovcombank.petbackendtransfers.exception.InsufficientFundsException;
import ru.sovcombank.petbackendtransfers.exception.UserNotFoundException;
import ru.sovcombank.petbackendtransfers.mapping.impl.MapToMakeTransferByAccountRequest;
import ru.sovcombank.petbackendtransfers.mapping.impl.MapToMakeTransferByPhoneRequest;
import ru.sovcombank.petbackendtransfers.mapping.impl.TransferToGetTransferResponse;
import ru.sovcombank.petbackendtransfers.model.api.request.MakeTransferByAccountRequest;
import ru.sovcombank.petbackendtransfers.model.api.request.MakeTransferByPhoneRequest;
import ru.sovcombank.petbackendtransfers.model.api.request.UpdateBalanceRequest;
import ru.sovcombank.petbackendtransfers.model.api.response.GetAccountResponse;
import ru.sovcombank.petbackendtransfers.model.api.response.GetAccountsResponse;
import ru.sovcombank.petbackendtransfers.model.api.response.GetTransferResponse;
import ru.sovcombank.petbackendtransfers.model.api.response.GetUserResponse;
import ru.sovcombank.petbackendtransfers.model.api.response.MakeTransferResponse;
import ru.sovcombank.petbackendtransfers.model.dto.AccountDTO;
import ru.sovcombank.petbackendtransfers.model.entity.Transfer;
import ru.sovcombank.petbackendtransfers.model.enums.CurEnum;
import ru.sovcombank.petbackendtransfers.model.enums.RequestTypeEnum;
import ru.sovcombank.petbackendtransfers.model.enums.TransferResponseMessagesEnum;
import ru.sovcombank.petbackendtransfers.model.enums.TypePaymentsEnum;
import ru.sovcombank.petbackendtransfers.repository.TransferRepository;
import ru.sovcombank.petbackendtransfers.service.builder.TransferService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TransferServiceImpl implements TransferService {

    private final TransferRepository transferRepository;
    private final UserServiceClient userServiceClient;
    private final AccountServiceClient accountServiceClient;
    private final CurrencyConverter currencyConverter;
    private final MapToMakeTransferByAccountRequest mapToMakeTransferByAccountRequest;
    private final MapToMakeTransferByPhoneRequest mapToMakeTransferByPhoneRequest;
    private final TransferToGetTransferResponse transferToGetTransferResponse;
    private final KafkaTemplate<String, Transfer> kafkaTemplate;

    public TransferServiceImpl(
            TransferRepository transferRepository,
            UserServiceClient userServiceClient,
            AccountServiceClient accountServiceClient,
            CurrencyConverter currencyConverter,
            MapToMakeTransferByAccountRequest mapToMakeTransferByAccountRequest,
            MapToMakeTransferByPhoneRequest mapToMakeTransferByPhoneRequest,
            TransferToGetTransferResponse transferToGetTransferResponse,
            KafkaTemplate<String, Transfer> kafkaTemplate
    ) {
        this.transferRepository = transferRepository;
        this.userServiceClient = userServiceClient;
        this.accountServiceClient = accountServiceClient;
        this.currencyConverter = currencyConverter;
        this.mapToMakeTransferByAccountRequest = mapToMakeTransferByAccountRequest;
        this.mapToMakeTransferByPhoneRequest = mapToMakeTransferByPhoneRequest;
        this.transferToGetTransferResponse = transferToGetTransferResponse;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Совершает перевод денежных средств.
     *
     * @param requestMap Запрос на перевод.
     * @return Ответ с сообщением о выполнении перевода.
     */
    @Override
    public MakeTransferResponse makeTransfer(Map<String, Object> requestMap) {
        String requestType = (String) requestMap.get("requestType");

        // Если перевод происходит по номеру счета
        if (RequestTypeEnum.ACCOUNT.getRequestType().equals(requestType)) {
            MakeTransferByAccountRequest makeTransferByAccountRequest =
                    mapToMakeTransferByAccountRequest.map(requestMap);
            return makeTransferByAccount(makeTransferByAccountRequest);
            // Если перевод происходит по номеру телефона
        } else if (RequestTypeEnum.PHONE.getRequestType().equals(requestType)) {
            MakeTransferByPhoneRequest makeTransferByPhoneRequest =
                    mapToMakeTransferByPhoneRequest.map(requestMap);
            return makeTransferByPhone(makeTransferByPhoneRequest);
        } else {
            throw new BadRequestException(TransferResponseMessagesEnum.BAD_REQUEST_FOR_REQUEST_TYPE.getMessage());
        }
    }

    /**
     * Совершает перевод по номеру счета.
     *
     * @param makeTransferByAccountRequest Запрос на перевод по номеру счета.
     * @return Ответ с сообщением о выполнении перевода.
     */
    @Transactional
    private MakeTransferResponse makeTransferByAccount(MakeTransferByAccountRequest makeTransferByAccountRequest) {
        checkRepeatNumbers(
                makeTransferByAccountRequest.getAccountNumberFrom(),
                makeTransferByAccountRequest.getAccountNumberTo(),
                TransferResponseMessagesEnum.BAD_REQUEST_FOR_ACCOUNT_NUMBER.getMessage());

        String clientIdFrom = makeTransferByAccountRequest.getClientId();
        validateUserForTransferByAccount(clientIdFrom);

        String accountNumberFrom = makeTransferByAccountRequest.getAccountNumberFrom();

        GetAccountResponse getAccountFromResponse = accountServiceClient.getAccountResponse(accountNumberFrom);

        validateAccountForTransfer(getAccountFromResponse);
        validateCur(makeTransferByAccountRequest.getCur(), getAccountFromResponse.getCur());

        BigDecimal transferAmount = makeTransferByAccountRequest.getAmount();
        validateSufficientFunds(accountNumberFrom, transferAmount);

        String accountNumberTo = makeTransferByAccountRequest.getAccountNumberTo();
        GetAccountResponse getAccountToResponse = validateAndGetAccount(accountNumberTo);

        String clientIdTo = getAccountToResponse.getClientId();

        BigDecimal amountByCur = getAmountByCur(
                makeTransferByAccountRequest.getCur(),
                transferAmount,
                getAccountToResponse
        );

        validateUserForTransferByAccount(clientIdTo);

        UpdateBalanceRequest updateBalanceRequestForAccountFrom = createUpdateBalanceRequest(
                TypePaymentsEnum.DEBITING.getTypePayment(),
                transferAmount
        );

        UpdateBalanceRequest updateBalanceRequestForAccountTo = createUpdateBalanceRequest(
                TypePaymentsEnum.REPLENISHMENT.getTypePayment(),
                amountByCur
        );

        updateAccountBalance(makeTransferByAccountRequest.getAccountNumberFrom(), updateBalanceRequestForAccountFrom);
        updateAccountBalance(makeTransferByAccountRequest.getAccountNumberTo(), updateBalanceRequestForAccountTo);

        Transfer transfer = createTransferObject(
                makeTransferByAccountRequest.getAccountNumberFrom(),
                makeTransferByAccountRequest.getAccountNumberTo(),
                transferAmount,
                makeTransferByAccountRequest.getCur()
        );

        saveTransfer(transfer);

        kafkaTemplate.send("transfers-history-transaction", transfer);

        return createMakeTransferResponse(accountServiceClient.getBalanceResponse(accountNumberFrom).getBalance());
    }

    /**
     * Совершает перевод по номеру телефона.
     *
     * @param makeTransferByPhoneRequest Запрос на перевод по номеру телефона.
     * @return Ответ с сообщением о выполнении перевода.
     */
    @Transactional
    private MakeTransferResponse makeTransferByPhone(MakeTransferByPhoneRequest makeTransferByPhoneRequest) {
        checkRepeatNumbers(
                makeTransferByPhoneRequest.getPhoneNumberFrom(),
                makeTransferByPhoneRequest.getPhoneNumberTo(),
                TransferResponseMessagesEnum.BAD_REQUEST_FOR_ACCOUNT_NUMBER.getMessage());

        String clientIdFrom = makeTransferByPhoneRequest.getClientId();
        String phoneNumberFrom = makeTransferByPhoneRequest.getPhoneNumberFrom();

        validateUserForTransferByPhone(clientIdFrom, phoneNumberFrom);

        GetAccountsResponse getAccountsResponseFrom = getAccountsResponse(clientIdFrom);
        String mainAccountFrom = getMainAccount(getAccountsResponseFrom.getAccountList());
        GetAccountResponse getAccountResponseFrom = getAccountResponse(mainAccountFrom);

        validateAccountForTransfer(getAccountResponseFrom);

        validateCur(makeTransferByPhoneRequest.getCur(), getAccountResponseFrom.getCur());

        BigDecimal amountByCur = getAmountByCur(
                makeTransferByPhoneRequest.getCur(),
                makeTransferByPhoneRequest.getAmount(),
                getAccountResponseFrom
        );

        validateSufficientFunds(mainAccountFrom, amountByCur);

        String phoneNumberTo = makeTransferByPhoneRequest.getPhoneNumberTo();
        GetUserResponse getUserResponse = userServiceClient.getUserInfo(phoneNumberTo);
        validateActiveUser(getUserResponse);

        GetAccountsResponse getAccountsResponseTo = getAccountsResponse(Integer.toString(getUserResponse.getId()));
        String mainAccountTo = getMainAccount(getAccountsResponseTo.getAccountList());

        BigDecimal transferAmount = makeTransferByPhoneRequest.getAmount();

        UpdateBalanceRequest updateBalanceRequestForAccountFrom = createUpdateBalanceRequest(
                TypePaymentsEnum.DEBITING.getTypePayment(),
                transferAmount
        );

        UpdateBalanceRequest updateBalanceRequestForAccountTo = createUpdateBalanceRequest(
                TypePaymentsEnum.REPLENISHMENT.getTypePayment(),
                amountByCur
        );

        updateAccountBalance(mainAccountFrom, updateBalanceRequestForAccountFrom);
        updateAccountBalance(mainAccountTo, updateBalanceRequestForAccountTo);

        Transfer transfer = createTransferObject(
                mainAccountFrom,
                mainAccountTo,
                transferAmount,
                makeTransferByPhoneRequest.getCur()
        );

        saveTransfer(transfer);

        kafkaTemplate.send("transfers-history-transaction", transfer);

        return createMakeTransferResponse(accountServiceClient.getBalanceResponse(mainAccountFrom).getBalance());
    }

    // Проверка на перевод самому себе
    private void checkRepeatNumbers(String numberFrom, String numberTo, String message) {
        if (numberTo.equals(numberFrom)) {
            throw new BadRequestException(message);
        }
    }

    // Валидация пользователя (проверка полей isActive и isDeleted)
    private void validateUserForTransferByAccount(String clientId) {
        if (!userServiceClient.checkUserExistsForTransferByAccount(clientId)) {
            throw new UserNotFoundException(TransferResponseMessagesEnum.USER_NOT_FOUND.getMessage());
        }
    }

    // Валидация счета (проверка поля isClosed)
    private void validateAccountForTransfer(GetAccountResponse getAccountFromResponse) {
        if (getAccountFromResponse.isClosed()) {
            throw new AccountClosedException(TransferResponseMessagesEnum.ACCOUNT_CLOSED.getMessage());
        }
    }

    // Валидация валюты (из запроса должна совпадать с фактической)
    private void validateCur(String curFromRequest, String realCur) {
        if (!curFromRequest.equals(realCur)) {
            throw new BadRequestException(TransferResponseMessagesEnum.BAD_REQUEST_FOR_CUR.getMessage());
        }
    }

    // Валидация суммы перевода (должна быть не больше, чем сумма на балансе)
    private void validateSufficientFunds(String accountNumber, BigDecimal transferAmount) {
        GetAccountResponse getAccountFromResponse = accountServiceClient.getAccountResponse(accountNumber);
        if (transferAmount.compareTo(getAccountFromResponse.getBalance()) > 0) {
            throw new InsufficientFundsException(TransferResponseMessagesEnum.INSUFFICIENT_FUNDS.getMessage());
        }
    }

    // Получение валидного ответа с информацией о счете
    private GetAccountResponse validateAndGetAccount(String accountNumberTo) {
        GetAccountResponse getAccountToResponse = accountServiceClient.getAccountResponse(accountNumberTo);
        if (!userServiceClient.checkUserExistsForTransferByAccount(getAccountToResponse.getClientId())) {
            throw new UserNotFoundException(TransferResponseMessagesEnum.USER_NOT_FOUND.getMessage());
        }
        return getAccountToResponse;
    }

    // Валидация клиента для перевода по номеру телефона
    // (проверка полей isActive, isDeleted и совпадение номера телефона)
    private void validateUserForTransferByPhone(String clientIdFrom, String phoneNumberFrom) {
        if (!userServiceClient.checkUserExistsForTransferByPhone(clientIdFrom, phoneNumberFrom)) {
            throw new UserNotFoundException(TransferResponseMessagesEnum.USER_NOT_FOUND.getMessage());
        }
    }

    // Получение ответа с информацией о счетах
    private GetAccountsResponse getAccountsResponse(String clientId) {
        return accountServiceClient.getAccountsResponse(clientId);
    }

    // Получение ответа с информацией о счете
    private GetAccountResponse getAccountResponse(String accountNumber) {
        return accountServiceClient.getAccountResponse(accountNumber);
    }

    // Получение ответа с информацией о счетах
    private void validateActiveUser(GetUserResponse getUserResponse) {
        if (!getUserResponse.isActive() || getUserResponse.isDeleted()) {
            throw new UserNotFoundException(TransferResponseMessagesEnum.USER_NOT_FOUND.getMessage());
        }
    }

    // Валидация клиента для перевода по номеру телефона (проверка полей isActive и isDeleted)
    private UpdateBalanceRequest createUpdateBalanceRequest(String typePayment, BigDecimal amount) {
        UpdateBalanceRequest updateBalanceRequest = new UpdateBalanceRequest();
        updateBalanceRequest.setTypePayments(typePayment);
        updateBalanceRequest.setAmount(amount);
        return updateBalanceRequest;
    }

    // Обновление баланса
    private void updateAccountBalance(String accountNumber, UpdateBalanceRequest updateBalanceRequest) {
        accountServiceClient.updateBalance(accountNumber, updateBalanceRequest);
    }

    // Создание сущности Transfer по значениям полей
    private Transfer createTransferObject(
            String accountNumberFrom,
            String accountNumberTo,
            BigDecimal transferAmount,
            String currency
    ) {
        Transfer transfer = new Transfer();
        transfer.setUuid(UUID.randomUUID());
        transfer.setAccountNumberFrom(accountNumberFrom);
        transfer.setAccountNumberTo(accountNumberTo);
        transfer.setAmount(transferAmount);
        transfer.setCur(currency);
        return transfer;
    }

    // Сохранение перевода в базе данных
    public void saveTransfer(Transfer transfer) {
        transferRepository.save(transfer);
    }

    // Получение ответа с информацией о совершенном переводе
    private MakeTransferResponse createMakeTransferResponse(BigDecimal balance) {
        return new MakeTransferResponse(
                TransferResponseMessagesEnum.TRANSFER_MAKED_SUCCESSFULLY.getMessage() + balance
        );
    }

    // Получение номера основного счета для совершения перевода по нему
    private String getMainAccount(List<AccountDTO> accountList) {
        for (AccountDTO accountDTO : accountList) {
            if (accountDTO.isMain()) {
                return accountDTO.getAccountNumber();
            }
        }
        throw new AccountNotFoundException(TransferResponseMessagesEnum.ACCOUNT_NOT_FOUND.getMessage());
    }

    // Получение суммы перевода после конвертации валют
    private BigDecimal getAmountByCur(String curFrom, BigDecimal amount, GetAccountResponse getAccountResponse) {

        String curTo = getAccountResponse.getCur();

        // Если валюты в счетах отправителя и получателя совпадают
        if (curFrom.equals(CurEnum.RUB.getCur()) && curTo.equals(CurEnum.RUB.getCur())) {
            return amount;
            // Если валюты в счетах отправителя и получателя не RUB (810)
        } else if (!curFrom.equals(CurEnum.RUB.getCur()) && !curTo.equals(CurEnum.RUB.getCur())) {
            return amount.multiply(BigDecimal.valueOf(
                    currencyConverter.getCurrentRate(curFrom) / currencyConverter.getCurrentRate(curTo))
            );
            // Если валюта счета отправителя RUB (810), а получателя - нет
        } else if (curFrom.equals(CurEnum.RUB.getCur())) {
            return amount.divide(BigDecimal.valueOf(currencyConverter.getCurrentRate(curTo)), 2, RoundingMode.HALF_UP);
            // Если валюта счета отправителя не RUB (810), а получателя - RUB
        } else {
            return amount.multiply(BigDecimal.valueOf(currencyConverter.getCurrentRate(curFrom)));
        }
    }

    /**
     * Получить информацию о транзакции по uuid.
     *
     * @param uuid uuid транзакции.
     * @return Ответ с информацией о транзакции.
     */
    @Override
    public GetTransferResponse getTransfers(String uuid) {
        Transfer transfer = transferRepository.findByUuid(UUID.fromString(uuid))
                .orElseThrow(() -> new AccountNotFoundException(
                        TransferResponseMessagesEnum.TRANSFER_NOT_FOUND.getMessage())
                );
        return transferToGetTransferResponse.map(transfer);
    }
}