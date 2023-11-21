package ru.sovcombank.petbackendaccounts.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sovcombank.petbackendaccounts.api.request.CreateAccountRequest;
import ru.sovcombank.petbackendaccounts.api.response.CreateAccountResponse;
import ru.sovcombank.petbackendaccounts.service.AccountService;

/**
 * Контроллер для управления счетами.
 */
@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    @Autowired
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
}