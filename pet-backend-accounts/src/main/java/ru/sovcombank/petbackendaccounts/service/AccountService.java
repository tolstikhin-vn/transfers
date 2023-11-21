package ru.sovcombank.petbackendaccounts.service;

import ru.sovcombank.petbackendaccounts.api.request.CreateAccountRequest;
import ru.sovcombank.petbackendaccounts.api.response.CreateAccountResponse;

public interface AccountService {

    CreateAccountResponse createAccount(CreateAccountRequest createAccountRequest);
}
