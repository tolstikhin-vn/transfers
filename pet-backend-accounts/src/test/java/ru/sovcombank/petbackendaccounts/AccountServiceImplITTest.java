package ru.sovcombank.petbackendaccounts;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.sovcombank.petbackendaccounts.client.UserServiceClient;
import ru.sovcombank.petbackendaccounts.exception.AccountNotFoundException;
import ru.sovcombank.petbackendaccounts.exception.BadRequestException;
import ru.sovcombank.petbackendaccounts.model.api.request.CreateAccountRequest;
import ru.sovcombank.petbackendaccounts.model.api.request.UpdateBalanceRequest;
import ru.sovcombank.petbackendaccounts.model.api.response.CreateAccountResponse;
import ru.sovcombank.petbackendaccounts.model.api.response.DeleteAccountResponse;
import ru.sovcombank.petbackendaccounts.model.api.response.GetAccountsResponse;
import ru.sovcombank.petbackendaccounts.model.api.response.GetBalanceResponse;
import ru.sovcombank.petbackendaccounts.model.api.response.UpdateBalanceResponse;
import ru.sovcombank.petbackendaccounts.model.enums.AccountResponseMessagesEnum;
import ru.sovcombank.petbackendaccounts.service.builder.AccountService;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AccountServiceImplITTest {
    @Container
    private static final PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:latest");

    @Autowired
    private AccountService accountService;

    @MockBean
    private UserServiceClient userServiceClient;

    @BeforeAll
    public static void startContainers() throws IOException {
        postgresContainer.start();
        System.setProperty("spring.datasource.url", postgresContainer.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgresContainer.getUsername());
        System.setProperty("spring.datasource.password", postgresContainer.getPassword());
    }

    @AfterAll
    static void stopContainer() {
        postgresContainer.stop();
    }

    @Test
    @DisplayName("Создание счета: успешный сценарий")
    public void createAccountSuccessTest() throws IOException {
        CreateAccountRequest createAccountRequest = readFromJson(
                "test-create-account-data.json",
                CreateAccountRequest.class);

        when(userServiceClient.checkUserExists(createAccountRequest.getClientId())).thenReturn(ResponseEntity.ok().build());

        CreateAccountResponse response = accountService.createAccount(createAccountRequest);

        assertNotNull(response);
        assertNotNull(response.getAccountNumber());
        assertEquals(AccountResponseMessagesEnum.ACCOUNT_CREATED_SUCCESSFULLY.getMessage(),
                response.getMessage());
    }

    @Test
    @DisplayName("Создание счета: ошибка BadRequestException")
    public void createAccountBadRequestTest() throws IOException {
        CreateAccountRequest createAccountRequest = readFromJson(
                "test-create-account-data.json",
                CreateAccountRequest.class);

        accountService.createAccount(createAccountRequest);
        accountService.createAccount(createAccountRequest);

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                accountService.createAccount(createAccountRequest));

        assertEquals(AccountResponseMessagesEnum.BAD_REQUEST_FOR_CUR.getMessage(),
                exception.getMessage());
    }

    @Test
    @DisplayName("Получение информации о счете: успешный сценарий")
    public void getAccountSuccessTest() throws IOException {
        CreateAccountRequest createAccountRequest = readFromJson(
                "test-create-account-data.json",
                CreateAccountRequest.class);

        accountService.createAccount(createAccountRequest);

        GetAccountsResponse getAccountsResponse = accountService.getAccounts(
                createAccountRequest.getClientId());

        assertNotNull(getAccountsResponse);
        assertEquals(1, getAccountsResponse.getAccountNumbers().size());
    }

    @Test
    @DisplayName("Удаление счета: успешный сценарий")
    public void deleteAccountSuccessTest() throws IOException {
        CreateAccountRequest createAccountRequest = readFromJson(
                "test-create-account-data.json",
                CreateAccountRequest.class);

        CreateAccountResponse createAccountResponse = accountService.createAccount(createAccountRequest);

        DeleteAccountResponse deleteAccountResponse = accountService.deleteAccount(
                createAccountResponse.getAccountNumber());

        assertNotNull(deleteAccountResponse);
        assertEquals(AccountResponseMessagesEnum.ACCOUNT_DELETED_SUCCESSFULLY.getMessage(),
                deleteAccountResponse.getMessage());
    }

    @Test
    @DisplayName("Удаление счета: ошибка AccountNotFoundException")
    public void deleteAccountsNotFoundTest() {
        String invalidAccountNumber = "4200810666000000";
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class,
                () -> accountService.deleteAccount(invalidAccountNumber));

        assertEquals(AccountResponseMessagesEnum.ACCOUNT_NOT_FOUND.getMessage(),
                exception.getMessage());
    }

    @Test
    @DisplayName("Получение баланса: успешный сценарий")
    public void getBalanceSuccessTest() throws IOException {
        CreateAccountRequest createAccountRequest = readFromJson(
                "test-create-account-data.json",
                CreateAccountRequest.class);

        CreateAccountResponse createAccountResponse = accountService.createAccount(createAccountRequest);

        GetBalanceResponse getBalanceResponse = accountService.getBalance(
                createAccountResponse.getAccountNumber());

        assertNotNull(getBalanceResponse);
        assertEquals(new BigDecimal("0.00"), getBalanceResponse.getBalance());
    }

    @Test
    @DisplayName("Получение баланса: ошибка AccountNotFoundException")
    public void getBalanceNotFoundTest() {
        String invalidAccountNumber = "4200810666000000";
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class,
                () -> accountService.getBalance(invalidAccountNumber));

        assertEquals(AccountResponseMessagesEnum.ACCOUNT_NOT_FOUND.getMessage(),
                exception.getMessage());
    }

    @Test
    @DisplayName("Изменение баланса: успешный сценарий")
    public void updateBalanceSuccessTest() throws IOException {
        CreateAccountRequest createAccountRequest = readFromJson(
                "test-create-account-data.json",
                CreateAccountRequest.class);

        CreateAccountResponse createAccountResponse = accountService.createAccount(createAccountRequest);

        UpdateBalanceRequest updateBalanceRequest = readFromJson(
                "test-update-balance-data.json",
                UpdateBalanceRequest.class);

        UpdateBalanceResponse updateBalanceResponse = accountService.updateBalance(
                createAccountResponse.getAccountNumber(),
                updateBalanceRequest);

        assertNotNull(updateBalanceResponse);
        assertEquals(AccountResponseMessagesEnum.BALANCE_UPDATED_SUCCESSFULLY.getMessage(),
                updateBalanceResponse.getMessage());
    }

    @Test
    @DisplayName("Изменение баланса: ошибка AccountNotFoundException")
    public void updateBalanceNotFoundTest() throws IOException {
        String invalidAccountNumber = "4200810666000000";
        UpdateBalanceRequest updateBalanceRequest = readFromJson(
                "test-update-balance-data.json",
                UpdateBalanceRequest.class);

        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class,
                () -> accountService.updateBalance(invalidAccountNumber, updateBalanceRequest));
        assertEquals(AccountResponseMessagesEnum.ACCOUNT_NOT_FOUND.getMessage(),
                exception.getMessage());
    }

    private <T> T readFromJson(String jsonFileName, Class<T> requestClass) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("json/" + jsonFileName);
        return objectMapper.readValue(inputStream, requestClass);
    }
}