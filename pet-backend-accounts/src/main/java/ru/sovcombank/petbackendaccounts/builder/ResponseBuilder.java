package ru.sovcombank.petbackendaccounts.builder;

import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendaccounts.model.api.response.CreateAccountResponse;
import ru.sovcombank.petbackendaccounts.model.entity.Account;
import ru.sovcombank.petbackendaccounts.model.enums.AccountResponseMessagesEnum;

@Component
public class ResponseBuilder {

    public CreateAccountResponse buildCreateAccountResponse(Account createdAccount) {
        CreateAccountResponse createAccountResponse = new CreateAccountResponse();
        createAccountResponse.setAccountNumber(createdAccount.getAccountNumber());
        createAccountResponse.setMessage(AccountResponseMessagesEnum.ACCOUNT_CREATED_SUCCESSFULLY.getMessage());
        return createAccountResponse;
    }
}
