package ru.sovcombank.petbackendaccounts.service.impl;

import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sovcombank.petbackendaccounts.builder.ResponseBuilder;
import ru.sovcombank.petbackendaccounts.client.UserServiceClient;
import ru.sovcombank.petbackendaccounts.exception.AccountNotFoundException;
import ru.sovcombank.petbackendaccounts.exception.BadRequestException;
import ru.sovcombank.petbackendaccounts.exception.UserNotFoundException;
import ru.sovcombank.petbackendaccounts.mapping.impl.AccountToGetAccountResponse;
import ru.sovcombank.petbackendaccounts.mapping.impl.CreateAccountRequestToAccount;
import ru.sovcombank.petbackendaccounts.mapping.impl.ListAccountToGetAccountsResponse;
import ru.sovcombank.petbackendaccounts.model.api.request.CreateAccountRequest;
import ru.sovcombank.petbackendaccounts.model.api.request.UpdateBalanceRequest;
import ru.sovcombank.petbackendaccounts.model.api.response.CreateAccountResponse;
import ru.sovcombank.petbackendaccounts.model.api.response.DeleteAccountResponse;
import ru.sovcombank.petbackendaccounts.model.api.response.GetAccountResponse;
import ru.sovcombank.petbackendaccounts.model.api.response.GetAccountsResponse;
import ru.sovcombank.petbackendaccounts.model.api.response.GetBalanceResponse;
import ru.sovcombank.petbackendaccounts.model.api.response.UpdateBalanceResponse;
import ru.sovcombank.petbackendaccounts.model.entity.Account;
import ru.sovcombank.petbackendaccounts.model.enums.AccountResponseMessagesEnum;
import ru.sovcombank.petbackendaccounts.model.enums.TypePaymentsEnum;
import ru.sovcombank.petbackendaccounts.repository.AccountRepository;
import ru.sovcombank.petbackendaccounts.service.AccountService;
import ru.sovcombank.petbackendaccounts.service.validator.AccountValidator;

import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Сервис для операций со счетами.
 */
@Slf4j
@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    private final ListAccountToGetAccountsResponse listAccountToGetAccountsResponse;

    private final CreateAccountRequestToAccount createAccountRequestToAccount;

    private final AccountToGetAccountResponse accountToGetAccountResponse;

    private final UserServiceClient userServiceClient;

    private final ResponseBuilder responseBuilder;

    private final AccountValidator accountValidator;

    private static final int MAX_ACCOUNTS_PER_CURRENCY = 2;

    public AccountServiceImpl(AccountRepository accountRepository,
                              ListAccountToGetAccountsResponse listAccountToGetAccountsResponse,
                              CreateAccountRequestToAccount createAccountRequestToAccount,
                              AccountToGetAccountResponse accountToGetAccountResponse,
                              UserServiceClient userServiceClient,
                              ResponseBuilder responseBuilder, AccountValidator accountValidator) {
        this.accountRepository = accountRepository;
        this.listAccountToGetAccountsResponse = listAccountToGetAccountsResponse;
        this.createAccountRequestToAccount = createAccountRequestToAccount;
        this.accountToGetAccountResponse = accountToGetAccountResponse;
        this.userServiceClient = userServiceClient;
        this.responseBuilder = responseBuilder;
        this.accountValidator = accountValidator;
    }

    /**
     * Создает новый счет для указанного клиента.
     *
     * @param createAccountRequest Запрос на создание счета.
     * @return Ответ с номером созданного счета и сообщением об успешном создании.
     * @throws UserNotFoundException если клиент не найден.
     * @throws BadRequestException   если достигнуто максимальное количество счетов для указанной валюты.
     */
    @Override
    @Transactional
    public CreateAccountResponse createAccount(CreateAccountRequest createAccountRequest) {

        Integer clientId = createAccountRequest.getClientId();

        // Проверка существования клиента с таким clientId
        userServiceClient.checkUserExists(clientId);

        // Проверка на лимит (2) кол-ва счетов в одной валюте для одного клиента
        if (!hasMaxAccountsForCurrency(clientId, createAccountRequest.getCur())) {
            String accountNumber = generateAccountNumber(createAccountRequest.getCur());

            Account accountEntity = createAccountRequestToAccount.map(createAccountRequest);
            accountEntity.setAccountNumber(accountNumber);
            accountEntity.setId(null);

            // Если это не первый счет у клиента - значение поля isMain становится false
            if (hasMoreThenOneAccount(clientId)) {
                accountEntity.setMain(false);
            }

            Account createdAccount = accountRepository.save(accountEntity);
            log.info("Account created successfully for client with id: {}", createAccountRequest.getClientId());

            return responseBuilder.buildCreateAccountResponse(createdAccount);
        } else {
            log.error("BadRequestException occurred: {}", AccountResponseMessagesEnum.BAD_REQUEST_FOR_CUR.getMessage());
            throw new BadRequestException(AccountResponseMessagesEnum.BAD_REQUEST_FOR_CUR.getMessage());
        }
    }

    // Генерирует уникальный номер счета на основе валюты.
    private String generateAccountNumber(String cur) {
        return String.format("4200%s666%06d", cur, new Random().nextInt(1000000));
    }

    // Проверяет, достигнуто ли максимальное количество счетов для указанной валюты у данного клиента.
    private boolean hasMaxAccountsForCurrency(Integer clientId, String cur) {
        List<Account> existingAccounts = accountRepository.findByClientIdAndCur(clientId, cur);
        return existingAccounts.size() >= MAX_ACCOUNTS_PER_CURRENCY;
    }

    // Проверяет, имеет ли клиент не менее одного счета.
    private boolean hasMoreThenOneAccount(Integer clientId) {
        Optional<List<Account>> existingAccounts = accountRepository.findByClientId(clientId);
        return existingAccounts.filter(accounts -> !accounts.isEmpty()).isPresent();
    }

    /**
     * Получает счета клиента.
     *
     * @param clientId id клиента.
     * @return Ответ с информацией о счетах.
     * @throws UserNotFoundException если клиент не найден.
     */
    @Override
    public GetAccountsResponse getAccounts(Integer clientId) {
        // Проверка существования клиента с таким clientId
        userServiceClient.checkUserExists(clientId);

        List<Account> accounts = accountRepository.findByClientId(clientId)
                .orElseThrow(() -> new UserNotFoundException(AccountResponseMessagesEnum.USER_NOT_FOUND.getMessage()));
        log.info("Retrieved accounts successfully for client with id: {}", clientId);
        return listAccountToGetAccountsResponse.map(accounts);
    }


    @Override
    public GetAccountResponse getAccountInfo(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(AccountResponseMessagesEnum.ACCOUNT_NOT_FOUND.getMessage()));
        log.info("Retrieved account information successfully for account with number: {}", accountNumber);
        return accountToGetAccountResponse.map(account);
    }

    /**
     * Удаляет счет (изменение поля isClosed на true).
     *
     * @param accountNumber Номер счета.
     * @return Ответ с сообщением.
     * @throws AccountNotFoundException В случае, если счет не найден.
     */
    @Override
    @Transactional
    public DeleteAccountResponse deleteAccount(String accountNumber) {
        // Проверяем существование клиента по переданному идентификатору
        Optional<Account> accountOptional = accountRepository.findByAccountNumber(accountNumber);
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();

            accountValidator.validateAccountIsClosed(accountOptional.get());

            account.setClosed(true);
            accountRepository.save(account);

            log.info("Account deleted successfully for account with number: {}", accountNumber);

            return new DeleteAccountResponse(AccountResponseMessagesEnum.ACCOUNT_DELETED_SUCCESSFULLY.getMessage());
        } else {
            log.error("AccountNotFoundException occurred: {}", AccountResponseMessagesEnum.ACCOUNT_NOT_FOUND.getMessage());
            throw new AccountNotFoundException(AccountResponseMessagesEnum.ACCOUNT_NOT_FOUND.getMessage());
        }
    }

    /**
     * Получает баланс клиента по номеру счета.
     *
     * @param accountNumber Номер счета.
     * @return Ответ с балансом.
     * @throws AccountNotFoundException В случае, если счет не найден.
     */
    @Override
    public GetBalanceResponse getBalance(String accountNumber) {
        Optional<Account> accountOptional = accountRepository.findByAccountNumber(accountNumber);
        if (accountOptional.isPresent()) {

            GetBalanceResponse getBalanceResponse = new GetBalanceResponse();
            getBalanceResponse.setBalance(accountOptional.get().getBalance());

            log.info("Retrieved balance successfully for account with number: {}", accountNumber);

            return getBalanceResponse;
        } else {
            log.error("AccountNotFoundException occurred: {}", AccountResponseMessagesEnum.ACCOUNT_NOT_FOUND.getMessage());
            throw new AccountNotFoundException(AccountResponseMessagesEnum.ACCOUNT_NOT_FOUND.getMessage());
        }
    }

    /**
     * Изменяет баланс.
     *
     * @param accountNumber        Номер счета.
     * @param updateBalanceRequest Запрос на изменение баланса.
     * @return Ответ с сообщением.
     * @throws UserNotFoundException В случае, если счет не найден.
     */
    @Override
    @Transactional
    @Retryable(retryFor = OptimisticLockException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public UpdateBalanceResponse updateBalance(String accountNumber, UpdateBalanceRequest updateBalanceRequest) {
        // Проверяем существование клиента по переданному идентификатору
        Optional<Account> accountOptional = accountRepository.findByAccountNumber(accountNumber);
        if (accountOptional.isPresent()) {
            Account changedAccount = makePayment(updateBalanceRequest, accountOptional.get());
            accountRepository.save(changedAccount);

            log.info("Balance updated successfully for account with number: {}", accountNumber);

            return new UpdateBalanceResponse(AccountResponseMessagesEnum.BALANCE_UPDATED_SUCCESSFULLY.getMessage());
        } else {
            log.error("AccountNotFoundException occurred: {}", AccountResponseMessagesEnum.ACCOUNT_NOT_FOUND.getMessage());
            throw new AccountNotFoundException(AccountResponseMessagesEnum.ACCOUNT_NOT_FOUND.getMessage());
        }
    }

    // Совершает операцию пополнения/снятия исходя из запроса.
    private Account makePayment(UpdateBalanceRequest updateBalanceRequest, Account account) {
        String typePayment = updateBalanceRequest.getTypePayments();

        // Совершаем действия в соответствии с выбранным типом операции
        if (typePayment.equals(TypePaymentsEnum.REPLENISHMENT.getTypePayment())) {
            account.setBalance(account.getBalance().add(updateBalanceRequest.getAmount()));
        } else if (typePayment.equals(TypePaymentsEnum.DEBITING.getTypePayment())) {
            accountValidator.validateAccountIsClosed(account);
            // Проверка на наличие достаточного количества средств для списания
            if (account.getBalance().compareTo(updateBalanceRequest.getAmount()) < 0) {
                throw new BadRequestException(AccountResponseMessagesEnum.BAD_REQUEST_FOR_AMOUNT.getMessage());
            }
            account.setBalance(account.getBalance().subtract(updateBalanceRequest.getAmount()));
        } else {
            throw new BadRequestException(AccountResponseMessagesEnum.BAD_REQUEST_FOR_TYPE_PAY.getMessage());
        }
        return account;
    }

    @Recover
    public void recover(OptimisticLockException ex) {
        throw new BadRequestException(AccountResponseMessagesEnum.BAD_REQUEST_FOR_AMOUNT.getMessage());
    }
}