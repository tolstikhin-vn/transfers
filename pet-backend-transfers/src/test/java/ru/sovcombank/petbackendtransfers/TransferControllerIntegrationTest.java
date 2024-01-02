package ru.sovcombank.petbackendtransfers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.sovcombank.petbackendtransfers.client.AccountServiceClient;
import ru.sovcombank.petbackendtransfers.client.UserServiceClient;
import ru.sovcombank.petbackendtransfers.model.api.response.GetAccountResponse;
import ru.sovcombank.petbackendtransfers.model.api.response.GetAccountsResponse;
import ru.sovcombank.petbackendtransfers.model.api.response.GetBalanceResponse;
import ru.sovcombank.petbackendtransfers.model.api.response.GetTransferResponse;
import ru.sovcombank.petbackendtransfers.model.api.response.GetUserResponse;
import ru.sovcombank.petbackendtransfers.model.api.response.MakeTransferResponse;
import ru.sovcombank.petbackendtransfers.model.api.response.MessageResponse;
import ru.sovcombank.petbackendtransfers.model.dto.TransferDTO;
import ru.sovcombank.petbackendtransfers.model.enums.TransferResponseMessagesEnum;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TransferControllerIntegrationTest {

    private final String BASE_HOST = "http://localhost:";

    @Container
    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserServiceClient userServiceClient;

    @MockBean
    private AccountServiceClient accountServiceClient;

    @MockBean
    private KafkaTemplate<String, TransferDTO> kafkaTemplate;

    @Value("${kafka.topic.transfers-history-transaction}")
    private String expectedKafkaTopic;


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
        jdbcTemplate.execute("TRUNCATE TABLE transfers");
    }

    @Test
    @DisplayName("Перевод средств по номеру счета: успешный сценарий")
    void makeTransferByAccountNumberSuccessfully() throws IOException {
        Map<String, Object> requestMap = readFromJson(
                "request/make-transfer-by-account-request.json",
                Map.class);

        MakeTransferResponse expectedResponse = readFromJson(
                "response/make-transfer-response.json",
                MakeTransferResponse.class);

        TransferDTO expectedTransfer = readFromJson(
                "entity/make-transfer-entity.json",
                TransferDTO.class);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<TransferDTO> transferCaptor = ArgumentCaptor.forClass(TransferDTO.class);

        when(userServiceClient.checkUserExistsForTransferByAccount(anyInt()))
                .thenReturn(true);

        when(accountServiceClient.getAccountResponse(anyString()))
                .thenReturn(readFromJson(
                        "response/get-account-response.json",
                        GetAccountResponse.class));

        when(accountServiceClient.getBalanceResponse(anyString()))
                .thenReturn(readFromJson(
                        "response/get-balance-response.json",
                        GetBalanceResponse.class));

        ResponseEntity<MakeTransferResponse> responseEntity = restTemplate.postForEntity(
                BASE_HOST + port + "/transfers",
                requestMap,
                MakeTransferResponse.class);

        MakeTransferResponse actualResponse = responseEntity.getBody();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getContentType().toString());
        assertEquals(expectedResponse, actualResponse);

        verify(kafkaTemplate).send(topicCaptor.capture(), transferCaptor.capture());

        String actualTopic = topicCaptor.getValue();
        TransferDTO actualTransfer = transferCaptor.getValue();

        expectedTransfer.setUuid(actualTransfer.getUuid());
        expectedTransfer.setTransactionDateTime(actualTransfer.getTransactionDateTime());

        assertEquals(expectedKafkaTopic, actualTopic);
        assertEquals(expectedTransfer, actualTransfer);
    }

    @Test
    @DisplayName("Перевод средств по номеру телефона: успешный сценарий")
    void makeTransferByPhoneNumberSuccessfully() throws IOException {
        Map<String, Object> requestMap = readFromJson(
                "request/make-transfer-by-phone-request.json",
                Map.class);

        MakeTransferResponse expectedResponse = readFromJson(
                "response/make-transfer-response.json",
                MakeTransferResponse.class);

        when(userServiceClient.checkUserExistsForTransferByPhone(anyInt(), anyString()))
                .thenReturn(true);

        when(accountServiceClient.getAccountResponse(anyString()))
                .thenReturn(readFromJson(
                        "response/get-account-response.json",
                        GetAccountResponse.class));

        when(userServiceClient.getUserInfo(anyString()))
                .thenReturn(readFromJson(
                        "response/get-user-response.json",
                        GetUserResponse.class));

        when(accountServiceClient.getAccountsResponse(anyInt()))
                .thenReturn(readFromJson(
                        "response/get-accounts-response.json",
                        GetAccountsResponse.class));

        when(accountServiceClient.getBalanceResponse(anyString()))
                .thenReturn(readFromJson(
                        "response/get-balance-response.json",
                        GetBalanceResponse.class));

        when(userServiceClient.checkUserExistsForTransferByAccount(anyInt()))
                .thenReturn(true);

        ResponseEntity<MakeTransferResponse> responseEntity = restTemplate.postForEntity(
                BASE_HOST + port + "/transfers",
                requestMap,
                MakeTransferResponse.class);

        MakeTransferResponse actualResponse = responseEntity.getBody();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getContentType().toString());
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    @DisplayName("Перевод средств по номеру телефона: ошибка UserNotFoundException")
    void makeTransferByPhoneNumberUserNotFoundException() throws IOException {
        Map<String, Object> requestMap = readFromJson(
                "request/make-transfer-by-phone-request.json",
                Map.class);

        ResponseEntity<MakeTransferResponse> responseEntity = restTemplate.postForEntity(
                BASE_HOST + port + "/transfers",
                requestMap,
                MakeTransferResponse.class);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getContentType().toString());
        assertEquals(TransferResponseMessagesEnum.USER_NOT_FOUND.getMessage(),
                Objects.requireNonNull(responseEntity.getBody()).getMessage());
    }

    @Test
    @DisplayName("Перевод средств по номеру телефона: ошибка AccountClosedException")
    void makeTransferByPhoneNumberAccountClosedException() throws IOException {
        Map<String, Object> requestMap = readFromJson(
                "request/make-transfer-by-phone-request.json",
                Map.class);

        when(userServiceClient.checkUserExistsForTransferByPhone(anyInt(), anyString()))
                .thenReturn(true);

        when(accountServiceClient.getAccountsResponse(anyInt()))
                .thenReturn(readFromJson(
                        "response/get-accounts-response.json",
                        GetAccountsResponse.class));

        when(accountServiceClient.getAccountResponse(anyString()))
                .thenReturn(readFromJson(
                        "response/get-closed-account-response.json",
                        GetAccountResponse.class));

        ResponseEntity<MakeTransferResponse> responseEntity = restTemplate.postForEntity(
                BASE_HOST + port + "/transfers",
                requestMap,
                MakeTransferResponse.class);

        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getContentType().toString());
        assertEquals(TransferResponseMessagesEnum.ACCOUNT_CLOSED.getMessage(),
                Objects.requireNonNull(responseEntity.getBody()).getMessage());
    }

    @Test
    @DisplayName("Перевод средств по номеру телефона: ошибка BadRequestException")
    void makeTransferByPhoneNumberBadRequestException() throws IOException {
        Map<String, Object> requestMap = readFromJson(
                "request/make-transfer-by-phone-request.json",
                Map.class);

        when(userServiceClient.checkUserExistsForTransferByPhone(anyInt(), anyString()))
                .thenReturn(true);

        when(accountServiceClient.getAccountsResponse(anyInt()))
                .thenReturn(readFromJson(
                        "response/get-accounts-response.json",
                        GetAccountsResponse.class));

        when(accountServiceClient.getAccountResponse(anyString()))
                .thenReturn(readFromJson(
                        "response/get-account-another-cur-response.json",
                        GetAccountResponse.class));

        ResponseEntity<MakeTransferResponse> responseEntity = restTemplate.postForEntity(
                BASE_HOST + port + "/transfers",
                requestMap,
                MakeTransferResponse.class);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getContentType().toString());
        assertEquals(TransferResponseMessagesEnum.BAD_REQUEST_FOR_CUR.getMessage(),
                Objects.requireNonNull(responseEntity.getBody()).getMessage());
    }

    @Test
    @DisplayName("Перевод средств по номеру телефона: ошибка InsufficientFundsException")
    void makeTransferByPhoneNumberInsufficientFundsException() throws IOException {
        Map<String, Object> requestMap = readFromJson(
                "request/make-transfer-by-phone-request.json",
                Map.class);

        when(userServiceClient.checkUserExistsForTransferByPhone(anyInt(), anyString()))
                .thenReturn(true);

        when(accountServiceClient.getAccountsResponse(anyInt()))
                .thenReturn(readFromJson(
                        "response/get-accounts-response.json",
                        GetAccountsResponse.class));

        when(accountServiceClient.getAccountResponse(anyString()))
                .thenReturn(readFromJson(
                        "response/get-account-no-balance-response.json",
                        GetAccountResponse.class));

        ResponseEntity<MakeTransferResponse> responseEntity = restTemplate.postForEntity(
                BASE_HOST + port + "/transfers",
                requestMap,
                MakeTransferResponse.class);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getContentType().toString());
        assertEquals(TransferResponseMessagesEnum.INSUFFICIENT_FUNDS.getMessage(),
                Objects.requireNonNull(responseEntity.getBody()).getMessage());
    }

    @Test
    @Sql("/sql/insert-transfer.sql")
    @DisplayName("Получение информации о переводе: успешный сценарий")
    void getInfoAboutTransactionSuccessfully() throws IOException {
        ResponseEntity<GetTransferResponse> responseEntity = restTemplate.getForEntity(
                BASE_HOST + port + "/transfers/14e6edca-6319-4d4a-99fc-b951323f78b8",
                GetTransferResponse.class);

        GetTransferResponse expectedResponse = readFromJson(
                "response/get-transfer-response.json",
                GetTransferResponse.class);

        GetTransferResponse actualResponse = responseEntity.getBody();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getContentType().toString());
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    @DisplayName("Получение информации о переводе: ошибка TransferNotFoundException")
    void getInfoAboutTransactionNotFound() {
        ResponseEntity<MessageResponse> responseEntity = restTemplate.getForEntity(
                BASE_HOST + port + "/transfers/14e6edca-6319-4d4a-99fc-b951323f78b8",
                MessageResponse.class);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getContentType().toString());
        assertEquals(TransferResponseMessagesEnum.TRANSFER_NOT_FOUND.getMessage(),
                Objects.requireNonNull(responseEntity.getBody()).getMessage());
    }

    private <T> T readFromJson(String jsonFileName, Class<T> requestClass) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("json/" + jsonFileName);
        return objectMapper.readValue(inputStream, requestClass);
    }
}
