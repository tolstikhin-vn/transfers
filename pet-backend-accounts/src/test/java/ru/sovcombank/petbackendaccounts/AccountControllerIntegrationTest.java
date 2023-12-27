package ru.sovcombank.petbackendaccounts;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.sovcombank.petbackendaccounts.client.UserServiceClient;
import ru.sovcombank.petbackendaccounts.model.api.request.CreateAccountRequest;
import ru.sovcombank.petbackendaccounts.model.api.request.UpdateBalanceRequest;
import ru.sovcombank.petbackendaccounts.model.api.response.CreateAccountResponse;
import ru.sovcombank.petbackendaccounts.model.api.response.DeleteAccountResponse;
import ru.sovcombank.petbackendaccounts.model.api.response.GetAccountsResponse;
import ru.sovcombank.petbackendaccounts.model.api.response.GetBalanceResponse;
import ru.sovcombank.petbackendaccounts.model.api.response.MessageResponse;
import ru.sovcombank.petbackendaccounts.model.api.response.UpdateBalanceResponse;
import ru.sovcombank.petbackendaccounts.model.dto.AccountDTO;
import ru.sovcombank.petbackendaccounts.model.enums.AccountResponseMessagesEnum;
import ru.sovcombank.petbackendaccounts.service.AccountService;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AccountControllerIntegrationTest {

    private final String BASE_HOST = "http://localhost:";

    @Container
    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest");

    @Autowired
    private AccountService accountService;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserServiceClient userServiceClient;

    @LocalServerPort
    private int port;

    @BeforeAll
    public static void startContainers() {
        postgresContainer.start();
        System.setProperty("spring.datasource.url", postgresContainer.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgresContainer.getUsername());
        System.setProperty("spring.datasource.password", postgresContainer.getPassword());
    }

    @AfterAll
    static void stopContainer() {
        postgresContainer.stop();
    }

    @AfterEach
    public void execute() {
        jdbcTemplate.execute("TRUNCATE TABLE accounts");
        jdbcTemplate.execute("ALTER SEQUENCE accounts_id_seq RESTART");
    }

    @Test
    @DisplayName("Создание счета: успешный сценарий")
    void createAccountSuccessfully() throws IOException {
        CreateAccountRequest createAccountRequest = readFromJson(
                "request/create-account-request.json",
                CreateAccountRequest.class);

        when(userServiceClient.checkUserExists(createAccountRequest.getClientId())).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<CreateAccountResponse> responseEntity = restTemplate.postForEntity(
                BASE_HOST + port + "/accounts",
                createAccountRequest,
                CreateAccountResponse.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getContentType().toString());
        assertNotNull(Objects.requireNonNull(responseEntity.getBody()).getAccountNumber());
        assertEquals(AccountResponseMessagesEnum.ACCOUNT_CREATED_SUCCESSFULLY.getMessage(),
                responseEntity.getBody().getMessage()
        );
    }

    @Test
    @DisplayName("Создание счета: ошибка BadRequestException")
    void createAccountBadRequestException() throws IOException {
        CreateAccountRequest createAccountRequest = readFromJson(
                "request/create-account-request.json",
                CreateAccountRequest.class);

        accountService.createAccount(createAccountRequest);
        accountService.createAccount(createAccountRequest);

        when(userServiceClient.checkUserExists(createAccountRequest.getClientId())).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<CreateAccountResponse> responseEntity = restTemplate.postForEntity(
                BASE_HOST + port + "/accounts",
                createAccountRequest,
                CreateAccountResponse.class);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getContentType().toString());
        assertEquals(AccountResponseMessagesEnum.BAD_REQUEST_FOR_CUR.getMessage(),
                Objects.requireNonNull(responseEntity.getBody()).getMessage()
        );
    }

    @Test
    @DisplayName("Получение информации о счете: успешный сценарий")
    void getAccountsByClientIdSuccessfully() throws IOException {
        CreateAccountRequest createAccountRequest = readFromJson(
                "request/create-account-request.json",
                CreateAccountRequest.class);

        accountService.createAccount(createAccountRequest);

        when(userServiceClient.checkUserExists(createAccountRequest.getClientId())).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<GetAccountsResponse> responseEntity = restTemplate.getForEntity(
                BASE_HOST + port + "/accounts/1",
                GetAccountsResponse.class);

        AccountDTO actualAccountDTO = responseEntity.getBody().getAccountNumbers().get(0);

        int actualAccountsListSize = Objects.requireNonNull(responseEntity.getBody()).getAccountNumbers().size();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getContentType().toString());
        assertEquals(1, actualAccountsListSize);

        assertNotNull(actualAccountDTO.getAccountNumber());
        assertEquals(createAccountRequest.getCur(), actualAccountDTO.getCur());
        assertTrue(actualAccountDTO.isMain());
    }

    @Test
    @DisplayName("Удаление счета: успешный сценарий")
    void deleteAccountSuccessfully() throws IOException {
        CreateAccountRequest createAccountRequest = readFromJson(
                "request/create-account-request.json",
                CreateAccountRequest.class);

        CreateAccountResponse createAccountResponse = accountService.createAccount(createAccountRequest);

        ResponseEntity<DeleteAccountResponse> responseEntity = restTemplate.exchange(
                BASE_HOST + port + "/accounts/" + createAccountResponse.getAccountNumber(),
                HttpMethod.DELETE,
                null,
                DeleteAccountResponse.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getContentType().toString());
        assertEquals(AccountResponseMessagesEnum.ACCOUNT_DELETED_SUCCESSFULLY.getMessage(),
                Objects.requireNonNull(responseEntity.getBody()).getMessage()
        );
    }

    @Test
    @DisplayName("Удаление счета: ошибка AccountNotFoundException")
    void deleteAccountAccountNotFoundException() {
        ResponseEntity<DeleteAccountResponse> responseEntity = restTemplate.exchange(
                BASE_HOST + port + "/accounts/77777777777777",
                HttpMethod.DELETE,
                null,
                DeleteAccountResponse.class);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getContentType().toString());
        assertEquals(AccountResponseMessagesEnum.ACCOUNT_NOT_FOUND.getMessage(),
                Objects.requireNonNull(responseEntity.getBody()).getMessage()
        );
    }

    @Test
    @DisplayName("Получение баланса: успешный сценарий")
    void getBalanceSuccessfully() throws IOException {
        CreateAccountRequest createAccountRequest = readFromJson(
                "request/create-account-request.json",
                CreateAccountRequest.class);

        CreateAccountResponse createAccountResponse = accountService.createAccount(createAccountRequest);

        ResponseEntity<GetBalanceResponse> responseEntity = restTemplate.getForEntity(
                BASE_HOST + port + "/accounts/balance/" + createAccountResponse.getAccountNumber(),
                GetBalanceResponse.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getContentType().toString());
        assertEquals(new BigDecimal("0.00"), Objects.requireNonNull(responseEntity.getBody()).getBalance());
    }

    @Test
    @DisplayName("Получение баланса: ошибка AccountNotFoundException")
    void getBalanceAccountNotFoundException() {
        ResponseEntity<MessageResponse> responseEntity = restTemplate.getForEntity(
                BASE_HOST + port + "/accounts/balance/77777777777777",
                MessageResponse.class);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getContentType().toString());
        assertEquals(AccountResponseMessagesEnum.ACCOUNT_NOT_FOUND.getMessage(),
                Objects.requireNonNull(Objects.requireNonNull(responseEntity.getBody()).getMessage())
        );
    }

    @Test
    @DisplayName("Изменение баланса: успешный сценарий")
    void updateBalanceSuccessfully() throws IOException {
        CreateAccountRequest createAccountRequest = readFromJson(
                "request/create-account-request.json",
                CreateAccountRequest.class);

        UpdateBalanceRequest updateBalanceRequest = readFromJson(
                "request/update-balance-request.json",
                UpdateBalanceRequest.class);

        CreateAccountResponse createAccountResponse = accountService.createAccount(createAccountRequest);

        ResponseEntity<UpdateBalanceResponse> responseEntity = restTemplate.exchange(
                BASE_HOST + port + "/accounts/balance/" + createAccountResponse.getAccountNumber(),
                HttpMethod.PUT,
                new HttpEntity<>(updateBalanceRequest),
                UpdateBalanceResponse.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getContentType().toString());
        assertEquals(AccountResponseMessagesEnum.BALANCE_UPDATED_SUCCESSFULLY.getMessage(),
                Objects.requireNonNull(responseEntity.getBody()).getMessage()
        );
    }

    @Test
    @DisplayName("Изменение баланса: ошибка AccountNotFoundException")
    void updateBalanceAccountNotFoundException() throws IOException {
        UpdateBalanceRequest updateBalanceRequest = readFromJson(
                "request/update-balance-request.json",
                UpdateBalanceRequest.class);

        ResponseEntity<MessageResponse> responseEntity = restTemplate.exchange(
                BASE_HOST + port + "/accounts/balance/77777777777777",
                HttpMethod.PUT,
                new HttpEntity<>(updateBalanceRequest),
                MessageResponse.class);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getContentType().toString());
        assertEquals(AccountResponseMessagesEnum.ACCOUNT_NOT_FOUND.getMessage(),
                Objects.requireNonNull(Objects.requireNonNull(responseEntity.getBody()).getMessage())
        );
    }

    private <T> T readFromJson(String jsonFileName, Class<T> requestClass) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("json/" + jsonFileName);
        return objectMapper.readValue(inputStream, requestClass);
    }
}