package ru.sovcombank.petbackendaccounts.service.validator;

import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendaccounts.exception.AccountNotFoundException;
import ru.sovcombank.petbackendaccounts.model.entity.Account;
import ru.sovcombank.petbackendaccounts.model.enums.AccountResponseMessagesEnum;
import ru.sovcombank.petbackendaccounts.repository.AccountRepository;

import java.util.List;
import java.util.Optional;

@Component
public class AccountValidator {

    private final AccountRepository accountRepository;

    private static final int MAX_ACCOUNTS_PER_CURRENCY = 2;

    public AccountValidator(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // Валидация счета (проверка поля isClosed)
    public void validateAccountIsClosed(Account account) {
        if (account.isClosed()) {
            throw new AccountNotFoundException(AccountResponseMessagesEnum.ACCOUNT_NOT_FOUND.getMessage());
        }
    }

    // Проверяет, достигнуто ли максимальное количество счетов для указанной валюты у данного клиента.
    public boolean hasMaxAccountsForCurrency(Integer clientId, String cur) {
        List<Account> existingAccounts = accountRepository.findByClientIdAndCur(clientId, cur);
        return existingAccounts.size() >= MAX_ACCOUNTS_PER_CURRENCY;
    }

    // Проверяет, имеет ли клиент не менее одного счета.
    public boolean hasMoreThenOneAccount(Integer clientId) {
        Optional<List<Account>> existingAccounts = accountRepository.findByClientId(clientId);
        return existingAccounts.filter(accounts -> !accounts.isEmpty()).isPresent();
    }
}
