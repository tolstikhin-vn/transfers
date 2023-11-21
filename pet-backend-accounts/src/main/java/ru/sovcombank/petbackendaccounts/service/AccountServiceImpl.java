package ru.sovcombank.petbackendaccounts.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.sovcombank.petbackendaccounts.api.request.CreateAccountRequest;
import ru.sovcombank.petbackendaccounts.api.response.CreateAccountResponse;
import ru.sovcombank.petbackendaccounts.exception.BadRequestException;
import ru.sovcombank.petbackendaccounts.exception.UserNotFoundException;
import ru.sovcombank.petbackendaccounts.mapper.AccountMapper;
import ru.sovcombank.petbackendaccounts.model.Account;
import ru.sovcombank.petbackendaccounts.repository.AccountRepository;

import java.util.List;
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
    private static final String BAD_REQUEST_MESSAGE_PREFIX = "Некорректный запрос по полю ";
    private static final String USER_NOT_FOUND_MESSAGE = "Не найден клиент по запросу";
    private static final String CUR_FIELD_NAME = "cur";
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
     * @throws BadRequestException  если достигнуто максимальное количество счетов для указанной валюты.
     */
    @Override
    public CreateAccountResponse createAccount(CreateAccountRequest createAccountRequest) {

        // Отправляем запрос для поиска клиента по id
        String getUserByIdUrl = "http://pet-backend-users:8081/users/" + createAccountRequest.getClientId();
        try {
            restTemplate.getForObject(getUserByIdUrl, Object.class);
        } catch (Exception ex) {
            throw new UserNotFoundException(USER_NOT_FOUND_MESSAGE);
        }

        // Проверка на лимит (2) кол-ва счетов в одной валюте для одного клиента
        if (!hasMaxAccountsForCurrency(Integer.parseInt(createAccountRequest.getClientId()), createAccountRequest.getCur())) {
            String accountNumber = generateAccountNumber(createAccountRequest.getCur());

            Account accountEntity = accountMapper.toEntity(createAccountRequest);
            System.out.println(accountEntity.toString());
            accountEntity.setAccountNumber(accountNumber);
            accountEntity.setId(null);

            Account createdAccount = accountRepository.save(accountEntity);
            return new CreateAccountResponse(createdAccount.getAccountNumber(), ACCOUNT_CREATED_SUCCESSFULLY_MESSAGE);
        } else {
            throw new BadRequestException(BAD_REQUEST_MESSAGE_PREFIX + CUR_FIELD_NAME);
        }
    }

    /**
     * Генерирует уникальный номер счета на основе валюты.
     *
     * @param cur Валюта счета.
     * @return Уникальный номер счета.
     */
    private String generateAccountNumber(String cur) {
        return "4200" + cur + "666" + String.format("%06d", new Random().nextInt(1000000));
    }

    /**
     * Проверяет, достигнуто ли максимальное количество счетов для указанной валюты у данного клиента.
     *
     * @param clientId Идентификатор клиента.
     * @param cur      Валюта счета.
     * @return true, если достигнуто максимальное количество счетов, иначе false.
     */
    private boolean hasMaxAccountsForCurrency(Integer clientId, String cur) {
        List<Account> existingAccounts = accountRepository.findByClientIdAndCur(clientId, cur);
        return existingAccounts.size() >= MAX_ACCOUNTS_PER_CURRENCY;
    }
}
