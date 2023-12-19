package ru.sovcombank.petbackendusers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
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
import ru.sovcombank.petbackendusers.model.api.request.CreateUserRequest;
import ru.sovcombank.petbackendusers.model.api.request.UpdateUserRequest;
import ru.sovcombank.petbackendusers.model.api.response.CreateUserResponse;
import ru.sovcombank.petbackendusers.model.api.response.DeleteUserResponse;
import ru.sovcombank.petbackendusers.model.api.response.GetUserResponse;
import ru.sovcombank.petbackendusers.model.api.response.MessageResponse;
import ru.sovcombank.petbackendusers.model.api.response.UpdateUserResponse;
import ru.sovcombank.petbackendusers.model.enums.UserMessagesEnum;
import ru.sovcombank.petbackendusers.service.builder.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserControllerIntegrationTest {

    private final String BASE_HOST = "http://localhost:";

    @Container
    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest");

    @Autowired
    private UserService userService;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

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
        jdbcTemplate.execute("TRUNCATE TABLE users");
        jdbcTemplate.execute("ALTER SEQUENCE users_id_seq RESTART");
    }

    @Test
    @DisplayName("Создание пользователя: успешный сценарий")
    void createUserSuccessfully() throws IOException {
        CreateUserRequest createUserRequest = readFromJson(
                "request/create-user-request.json",
                CreateUserRequest.class);

        ResponseEntity<CreateUserResponse> responseEntity = restTemplate.postForEntity(
                BASE_HOST + port + "/users/new",
                createUserRequest,
                CreateUserResponse.class);

        CreateUserResponse expectedResponse = readFromJson(
                "response/create-user-response.json",
                CreateUserResponse.class);

        CreateUserResponse actualResponse = responseEntity.getBody();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getContentType().toString());
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    @DisplayName("Создание пользователя: ошибка ConflictException")
    void createUserConflictException() throws IOException {
        CreateUserRequest createUserRequest = readFromJson(
                "request/create-user-request.json",
                CreateUserRequest.class);

        userService.createUser(createUserRequest);

        ResponseEntity<MessageResponse> responseEntity = restTemplate.postForEntity(
                BASE_HOST + port + "/users/new",
                createUserRequest,
                MessageResponse.class);

        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getContentType().toString());
        assertEquals(createUserRequest.getPhoneNumber() + " с таким phone_number уже зарегистрирован",
                Objects.requireNonNull(responseEntity.getBody()).getMessage());
    }

    @Test
    @DisplayName("Получение информации о клиенте по id: успешный сценарий")
    void getUserByIdSuccessfully() throws IOException {
        CreateUserRequest createUserRequest = readFromJson(
                "request/create-user-request.json",
                CreateUserRequest.class);

        userService.createUser(createUserRequest);

        ResponseEntity<GetUserResponse> responseEntity = restTemplate.getForEntity(
                BASE_HOST + port + "/users/1",
                GetUserResponse.class);

        GetUserResponse expectedResponse = readFromJson(
                "response/get-user-response.json",
                GetUserResponse.class);

        GetUserResponse actualResponse = responseEntity.getBody();
        actualResponse.setCreateDateTime(expectedResponse.getCreateDateTime());

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getContentType().toString());
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    @DisplayName("Получение информации о клиенте по id: ошибка UserNotFoundException")
    void getUserByIdUserNotFoundException() {
        ResponseEntity<MessageResponse> responseEntity = restTemplate.getForEntity(
                BASE_HOST + port + "/users/999",
                MessageResponse.class);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getContentType().toString());
        assertEquals(UserMessagesEnum.USER_NOT_FOUND_MESSAGE.getMessage(),
                Objects.requireNonNull(responseEntity.getBody()).getMessage());
    }

    @Test
    @DisplayName("Получение информации о клиенте по номеру телефона: успешный сценарий")
    void getUserByPhoneNumberSuccessfully() throws IOException {
        CreateUserRequest createUserRequest = readFromJson(
                "request/create-user-request.json",
                CreateUserRequest.class);

        userService.createUser(createUserRequest);

        ResponseEntity<GetUserResponse> responseEntity = restTemplate.getForEntity(
                BASE_HOST + port + "/users/phone-number/" + createUserRequest.getPhoneNumber(),
                GetUserResponse.class);

        GetUserResponse expectedResponse = readFromJson(
                "response/get-user-response.json",
                GetUserResponse.class);

        GetUserResponse actualResponse = responseEntity.getBody();
        actualResponse.setCreateDateTime(expectedResponse.getCreateDateTime());

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getContentType().toString());
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    @DisplayName("Получение информации о клиенте по номеру телефона: ошибка UserNotFoundException")
    void getUserByPhoneNumberUserNotFoundException() {
        ResponseEntity<MessageResponse> responseEntity = restTemplate.getForEntity(
                BASE_HOST + port + "/users/phone-number/70000000000",
                MessageResponse.class);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getContentType().toString());
        assertEquals(UserMessagesEnum.USER_NOT_FOUND_MESSAGE.getMessage(),
                Objects.requireNonNull(responseEntity.getBody()).getMessage());
    }

    @Test
    @DisplayName("Изменение данных клиента: успешный сценарий")
    void updateUserSuccessfully() throws IOException {
        CreateUserRequest createUserRequest = readFromJson(
                "request/create-user-request.json",
                CreateUserRequest.class);

        CreateUserResponse createUserResponse = userService.createUser(createUserRequest);

        UpdateUserRequest updateUserRequest = readFromJson(
                "request/update-user-request.json",
                UpdateUserRequest.class);

        ResponseEntity<UpdateUserResponse> responseEntity = restTemplate.exchange(
                BASE_HOST + port + "/users/" + createUserResponse.getClientId(),
                HttpMethod.PUT,
                new HttpEntity<>(updateUserRequest),
                UpdateUserResponse.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getContentType().toString());
        assertEquals(UserMessagesEnum.USER_UPDATED_SUCCESSFULLY_MESSAGE.getMessage(),
                Objects.requireNonNull(responseEntity.getBody()).getMessage());
    }

    @Test
    @DisplayName("Изменение данных клиента: ошибка UserNotFoundException")
    void updateUserUserNotFoundException() throws IOException {
        UpdateUserRequest updateUserRequest = readFromJson(
                "request/update-user-request.json",
                UpdateUserRequest.class);

        ResponseEntity<UpdateUserResponse> responseEntity = restTemplate.exchange(
                BASE_HOST + port + "/users/999",
                HttpMethod.PUT,
                new HttpEntity<>(updateUserRequest),
                UpdateUserResponse.class);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getContentType().toString());
        assertEquals(UserMessagesEnum.USER_NOT_FOUND_MESSAGE.getMessage(),
                Objects.requireNonNull(responseEntity.getBody()).getMessage());
    }

    @Test
    @DisplayName("Удаление клиента: успешный сценарий")
    void deleteUserSuccessfully() throws IOException {
        CreateUserRequest createUserRequest = readFromJson(
                "request/create-user-request.json",
                CreateUserRequest.class);

        CreateUserResponse createUserResponse = userService.createUser(createUserRequest);

        ResponseEntity<DeleteUserResponse> responseEntity = restTemplate.exchange(
                BASE_HOST + port + "/users/" + createUserResponse.getClientId(),
                HttpMethod.DELETE,
                null,
                DeleteUserResponse.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getContentType().toString());
        assertEquals(UserMessagesEnum.USER_DELETED_SUCCESSFULLY_MESSAGE.getMessage(),
                Objects.requireNonNull(responseEntity.getBody()).getMessage());
    }

    @Test
    @DisplayName("Удаление клиента: ошибка UserNotFoundException")
    void deleteUserUserNotFoundException() {
        ResponseEntity<DeleteUserResponse> responseEntity = restTemplate.exchange(
                BASE_HOST + port + "/users/1",
                HttpMethod.DELETE,
                null,
                DeleteUserResponse.class);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getContentType().toString());
        assertEquals(UserMessagesEnum.USER_NOT_FOUND_MESSAGE.getMessage(),
                Objects.requireNonNull(responseEntity.getBody()).getMessage());
    }

    private <T> T readFromJson(String jsonFileName, Class<T> requestClass) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("json/" + jsonFileName);
        return objectMapper.readValue(inputStream, requestClass);
    }
}
