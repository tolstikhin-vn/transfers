package ru.sovcombank.petbackendaccounts.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sovcombank.petbackendaccounts.model.api.request.CreateAccountRequest;
import ru.sovcombank.petbackendaccounts.model.api.request.UpdateBalanceRequest;
import ru.sovcombank.petbackendaccounts.model.api.response.CreateAccountResponse;
import ru.sovcombank.petbackendaccounts.model.api.response.DeleteAccountResponse;
import ru.sovcombank.petbackendaccounts.model.api.response.GetAccountsResponse;
import ru.sovcombank.petbackendaccounts.model.api.response.GetBalanceResponse;
import ru.sovcombank.petbackendaccounts.model.api.response.UpdateBalanceResponse;
import ru.sovcombank.petbackendaccounts.service.builder.AccountService;

/**
 * Контроллер для управления счетами.
 */
@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Обрабатывает запрос на создание нового счета.
     *
     * @param createAccountRequest Запрос на создание счета.
     * @return Ответ с результатом создания счета.
     */
    @PostMapping("/new")
    public ResponseEntity<Object> createUser(@Valid @RequestBody CreateAccountRequest createAccountRequest) {
        CreateAccountResponse response = accountService.createAccount(createAccountRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Обрабатывает запрос на получение счетов клиента по id клиента.
     *
     * @param clientId Идентификатор клиента.
     * @return Ответ с информацией о счетах.
     */
    @GetMapping("/{clientId}")
    public ResponseEntity<Object> getAccountsByClientId(@PathVariable String clientId) {
        GetAccountsResponse response = accountService.getAccounts(clientId);
        return ResponseEntity.ok(response);
    }

    /**
     * Обрабатывает запрос на удаление счета.
     *
     * @param accountNumber Номер счета.
     * @return Ответ с результатом удаления счета.
     */
    @DeleteMapping("/{accountNumber}")
    public ResponseEntity<Object> deleteAccount(@PathVariable String accountNumber) {
        DeleteAccountResponse response = accountService.deleteAccount(accountNumber);
        return ResponseEntity.ok(response);
    }

    /**
     * Обрабатывает запрос на получение баланса.
     *
     * @param accountNumber Номер счета.
     * @return Ответ с балансом.
     */
    @GetMapping("/balance/{accountNumber}")
    public ResponseEntity<Object> getBalance(@PathVariable String accountNumber) {
        GetBalanceResponse response = accountService.getBalance(accountNumber);
        return ResponseEntity.ok(response);
    }

    /**
     * Обрабатывает запрос на изменение/пополнение баланса.
     *
     * @param accountNumber        Номер счета.
     * @param updateBalanceRequest Запрос на изменение баланса.
     * @return Ответ с результатом изменения баланса.
     */
    @PutMapping("/balance/{accountNumber}")
    public ResponseEntity<Object> updateUser(@PathVariable String accountNumber, @Valid @RequestBody UpdateBalanceRequest updateBalanceRequest) {
        UpdateBalanceResponse response = accountService.updateBalance(accountNumber, updateBalanceRequest);
        return ResponseEntity.ok(response);
    }
}