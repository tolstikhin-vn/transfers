package ru.sovcombank.petbackendaccounts.service;

import ru.sovcombank.petbackendaccounts.api.request.CreateAccountRequest;
import ru.sovcombank.petbackendaccounts.api.request.UpdateBalanceRequest;
import ru.sovcombank.petbackendaccounts.api.response.CreateAccountResponse;
import ru.sovcombank.petbackendaccounts.api.response.DeleteAccountResponse;
import ru.sovcombank.petbackendaccounts.api.response.GetAccountsResponse;
import ru.sovcombank.petbackendaccounts.api.response.GetBalanceResponse;
import ru.sovcombank.petbackendaccounts.api.response.UpdateBalanceResponse;

public interface AccountService {

    CreateAccountResponse createAccount(CreateAccountRequest createAccountRequest);
    GetAccountsResponse getAccounts(String clientId);
    DeleteAccountResponse deleteAccount(String accountNumber);
    GetBalanceResponse getBalance(String accountNumber);
    UpdateBalanceResponse updateBalance(String accountNumber, UpdateBalanceRequest updateBalanceRequest);
}
