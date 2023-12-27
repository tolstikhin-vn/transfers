package ru.sovcombank.petbackendaccounts.service;

import ru.sovcombank.petbackendaccounts.model.api.request.CreateAccountRequest;
import ru.sovcombank.petbackendaccounts.model.api.request.UpdateBalanceRequest;
import ru.sovcombank.petbackendaccounts.model.api.response.CreateAccountResponse;
import ru.sovcombank.petbackendaccounts.model.api.response.DeleteAccountResponse;
import ru.sovcombank.petbackendaccounts.model.api.response.GetAccountResponse;
import ru.sovcombank.petbackendaccounts.model.api.response.GetAccountsResponse;
import ru.sovcombank.petbackendaccounts.model.api.response.GetBalanceResponse;
import ru.sovcombank.petbackendaccounts.model.api.response.UpdateBalanceResponse;

public interface AccountService {

    CreateAccountResponse createAccount(CreateAccountRequest createAccountRequest);

    GetAccountsResponse getAccounts(String clientId);

    GetAccountResponse getAccountInfo(String accountNumber);

    DeleteAccountResponse deleteAccount(String accountNumber);

    GetBalanceResponse getBalance(String accountNumber);

    UpdateBalanceResponse updateBalance(String accountNumber, UpdateBalanceRequest updateBalanceRequest);
}
