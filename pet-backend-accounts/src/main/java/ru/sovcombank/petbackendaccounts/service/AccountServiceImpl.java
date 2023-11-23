package ru.sovcombank.petbackendaccounts.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.sovcombank.petbackendaccounts.api.request.CreateAccountRequest;
import ru.sovcombank.petbackendaccounts.api.request.UpdateBalanceRequest;
import ru.sovcombank.petbackendaccounts.api.response.CreateAccountResponse;
import ru.sovcombank.petbackendaccounts.api.response.DeleteAccountResponse;
import ru.sovcombank.petbackendaccounts.api.response.GetAccountsResponse;
import ru.sovcombank.petbackendaccounts.api.response.GetBalanceResponse;
import ru.sovcombank.petbackendaccounts.api.response.UpdateBalanceResponse;
import ru.sovcombank.petbackendaccounts.enums.TypePaymentsEnum;
import ru.sovcombank.petbackendaccounts.exception.AccountNotFoundException;
import ru.sovcombank.petbackendaccounts.exception.BadRequestException;
import ru.sovcombank.petbackendaccounts.exception.UserNotFoundException;
import ru.sovcombank.petbackendaccounts.mapper.AccountMapper;
import ru.sovcombank.petbackendaccounts.model.Account;
import ru.sovcombank.petbackendaccounts.repository.AccountRepository;

import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Сервис для операций со счетами.
 */
@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final RestTemplate restTemplate;
    private static final String ACCOUNT_CREATED_SUCCESSFULLY_MESSAGE = "Счет успешно создан";
    private static final String BAD_REQUEST_FOR_CUR_MESSAGE = "Некорректный запрос по полю cur";
    private static final String BAD_REQUEST_FOR_TYPE_PAY_MESSAGE = "Некорректный запрос по полю typePayments";
    private static final String BAD_REQUEST_FOR_AMOUNT_MESSAGE = "Некорректный запрос по полю amount";
    private static final String USER_NOT_FOUND_MESSAGE = "Не найден клиент по запросу";
    private static final String ACCOUNT_NOT_FOUND_MESSAGE = "Не найден счет по запросу";
    private static final String ACCOUNT_DELETED_SUCCESSFULLY_MESSAGE = "Счет успешно закрыт";
    private static final String BALANCE_UPDATED_SUCCESSFULLY_MESSAGE = "Баланс успешно обновлен";
    private static final int MAX_ACCOUNTS_PER_CURRENCY = 2;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository,
                              AccountMapper accountMapper,
                              RestTemplate restTemplate) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
        this.restTemplate = restTemplate;
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
    public CreateAccountResponse createAccount(CreateAccountRequest createAccountRequest) {

        // Отправляем запрос для поиска клиента по id
        Integer clientId = Integer.parseInt(createAccountRequest.getClientId());
        String getUserByIdUrl = "http://pet-backend-users:8081/users/" + clientId;

        try {
            restTemplate.getForObject(getUserByIdUrl, Object.class);
        } catch (Exception ex) {
            throw new UserNotFoundException(USER_NOT_FOUND_MESSAGE);
        }

        // Проверка на лимит (2) кол-ва счетов в одной валюте для одного клиента
        if (!hasMaxAccountsForCurrency(clientId, createAccountRequest.getCur())) {
            String accountNumber = generateAccountNumber(createAccountRequest.getCur());

            Account accountEntity = accountMapper.toEntity(createAccountRequest);
            accountEntity.setAccountNumber(accountNumber);
            accountEntity.setId(null);

            // Если это не первый счет у клиента - значение поля isMain становится false
            if (hasMoreThenOneAccount(clientId)) {
                accountEntity.setMain(false);
            }

            Account createdAccount = accountRepository.save(accountEntity);
            return new CreateAccountResponse(createdAccount.getAccountNumber(), ACCOUNT_CREATED_SUCCESSFULLY_MESSAGE);
        } else {
            throw new BadRequestException(BAD_REQUEST_FOR_CUR_MESSAGE);
        }
    }

    /**
     * Получает счета клиента.
     *
     * @param clientId id клиента.
     * @return Ответ с информацией о счетах.
     * @throws UserNotFoundException если клиент не найден.
     */
    @Override
    public GetAccountsResponse getAccounts(String clientId) {
        // Отправляем запрос для поиска клиента по id
        String getUserByIdUrl = "http://pet-backend-users:8081/users/" + clientId;
        try {
            restTemplate.getForObject(getUserByIdUrl, Object.class);
        } catch (Exception ex) {
            throw new UserNotFoundException(USER_NOT_FOUND_MESSAGE);
        }

        List<Account> accounts = accountRepository.findByClientId(Integer.parseInt(clientId))
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MESSAGE));

        return accountMapper.toGetAccountsResponse(accounts, clientId);
    }

    /**
     * Удаляет счет (изменение поля isClosed на true).
     *
     * @param accountNumber Номер счета.
     * @return Ответ с сообщением.
     * @throws AccountNotFoundException В случае, если счет не найден.
     */
    @Override
    public DeleteAccountResponse deleteAccount(String accountNumber) {
        // Проверяем существование клиента по переданному идентификатору
        Optional<Account> accountOptional = accountRepository.findByAccountNumber(accountNumber);
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            account.setClosed(true);
            accountRepository.save(account);
            return new DeleteAccountResponse(ACCOUNT_DELETED_SUCCESSFULLY_MESSAGE);
        } else {
            throw new AccountNotFoundException(ACCOUNT_NOT_FOUND_MESSAGE);
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
            return new GetBalanceResponse(accountOptional.get().getBalance());
        } else {
            throw new AccountNotFoundException(ACCOUNT_NOT_FOUND_MESSAGE);
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
    public UpdateBalanceResponse updateBalance(String accountNumber, UpdateBalanceRequest updateBalanceRequest) {
        // Проверяем существование клиента по переданному идентификатору
        Optional<Account> accountOptional = accountRepository.findByAccountNumber(accountNumber);
        if (accountOptional.isPresent()) {
            Account changedAccount = makePayment(updateBalanceRequest, accountOptional.get());
            accountRepository.save(changedAccount);
            return new UpdateBalanceResponse(BALANCE_UPDATED_SUCCESSFULLY_MESSAGE);
        } else {
            throw new AccountNotFoundException(ACCOUNT_NOT_FOUND_MESSAGE);
        }
    }

    // Генерирует уникальный номер счета на основе валюты.
    private String generateAccountNumber(String cur) {
        return "4200" + cur + "666" + String.format("%06d", new Random().nextInt(1000000));
    }

    // Проверяет, достигнуто ли максимальное количество счетов для указанной валюты у данного клиента.
    private boolean hasMaxAccountsForCurrency(Integer clientId, String cur) {
        List<Account> existingAccounts = accountRepository.findByClientIdAndCur(clientId, cur);
        return existingAccounts.size() >= MAX_ACCOUNTS_PER_CURRENCY;
    }

    // Проверяет, имеет ли клиент не менее одного счета.
    private boolean hasMoreThenOneAccount(Integer clientId) {
        Optional<List<Account>> existingAccounts = accountRepository.findByClientId(clientId);
        return existingAccounts.filter(accounts -> accounts.size() >= 1).isPresent();
    }

    // Совершает операцию пополнения/снятия исходя из запроса.
    private Account makePayment(UpdateBalanceRequest updateBalanceRequest, Account account) {
        String typePayment = updateBalanceRequest.getTypePayments();

        // Совершаем действия в соответствии с выбранным типом операции
        if (typePayment.equals(TypePaymentsEnum.REPLENISHMENT.getTypePayment())) {
            account.setBalance(account.getBalance().add(updateBalanceRequest.getAmount()));
        } else if (typePayment.equals(TypePaymentsEnum.DEBITING.getTypePayment())) {
            // Проверка на наличие достаточного количества средств для списания
            if (account.getBalance().compareTo(updateBalanceRequest.getAmount()) < 0) {
                throw new BadRequestException(BAD_REQUEST_FOR_AMOUNT_MESSAGE);
            }
            account.setBalance(account.getBalance().subtract(updateBalanceRequest.getAmount()));
        } else {
            throw new BadRequestException(BAD_REQUEST_FOR_TYPE_PAY_MESSAGE);
        }
        return account;
    }
}