package ru.sovcombank.petbackendaccounts.service.validator;

import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendaccounts.exception.AccountNotFoundException;
import ru.sovcombank.petbackendaccounts.model.entity.Account;
import ru.sovcombank.petbackendaccounts.model.enums.AccountResponseMessagesEnum;

@Component
public class AccountValidator {

    // Валидация счета (проверка поля isClosed)
    public void validateAccountIsClosed(Account account) {
        if (account.isClosed()) {
            throw new AccountNotFoundException(AccountResponseMessagesEnum.ACCOUNT_NOT_FOUND.getMessage());
        }
    }
}
