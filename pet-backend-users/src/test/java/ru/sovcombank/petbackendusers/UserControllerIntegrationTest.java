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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void createUser_Successfully() throws IOException {
        CreateUserRequest createUserRequest = readFromJson(
                "test-create-user-data.json",
                CreateUserRequest.class);

        ResponseEntity<CreateUserResponse> responseEntity = restTemplate.postForEntity(
                BASE_HOST + port + "/users/new",
                createUserRequest,
                CreateUserResponse.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("1", Objects.requireNonNull(responseEntity.getBody()).getClientId());
        assertEquals(UserMessagesEnum.USER_CREATED_SUCCESSFULLY_MESSAGE.getMessage(),
                responseEntity.getBody().getMessage());
    }

    @Test
    @DisplayName("Создание пользователя: ошибка ConflictException")
    void createUser_ConflictException() throws IOException {
        CreateUserRequest createUserRequest = readFromJson(
                "test-create-user-data.json",
                CreateUserRequest.class);

        userService.createUser(createUserRequest);

        ResponseEntity<MessageResponse> responseEntity = restTemplate.postForEntity(
                BASE_HOST + port + "/users/new",
                createUserRequest,
                MessageResponse.class);

        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        assertEquals(createUserRequest.getPhoneNumber() + " с таким phone_number уже зарегистрирован",
                Objects.requireNonNull(responseEntity.getBody()).getMessage());
    }

    @Test
    @DisplayName("Получение информации о клиенте по id: успешный сценарий")
    void getUserById_Successfully() throws IOException {
        CreateUserRequest createUserRequest = readFromJson(
                "test-create-user-data.json",
                CreateUserRequest.class);

        userService.createUser(createUserRequest);

        ResponseEntity<GetUserResponse> responseEntity = restTemplate.getForEntity(
                BASE_HOST + port + "/users/1",
                GetUserResponse.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(1, Objects.requireNonNull(responseEntity.getBody()).getId());
        assertEquals(createUserRequest.getLastName(), Objects.requireNonNull(responseEntity.getBody()).getLastName());
        assertEquals(createUserRequest.getFirstName(), Objects.requireNonNull(responseEntity.getBody()).getFirstName());
        assertEquals(createUserRequest.getFatherName(), Objects.requireNonNull(responseEntity.getBody()).getFatherName());
        assertEquals(createUserRequest.getPhoneNumber(), Objects.requireNonNull(responseEntity.getBody()).getPhoneNumber());
        assertEquals(createUserRequest.getBirthDate(), Objects.requireNonNull(responseEntity.getBody()).getBirthDate());
        assertEquals(createUserRequest.getPassportNumber(), Objects.requireNonNull(responseEntity.getBody()).getPassportNumber());
        assertEquals(createUserRequest.getEmail(), responseEntity.getBody().getEmail());
        assertNotNull(responseEntity.getBody().getCreateDateTime());
        assertTrue(responseEntity.getBody().isActive());
        assertFalse(responseEntity.getBody().isDeleted());
    }

    @Test
    @DisplayName("Получение информации о клиенте по id: ошибка UserNotFoundException")
    void getUserById_UserNotFoundException() {
        ResponseEntity<MessageResponse> responseEntity = restTemplate.getForEntity(
                BASE_HOST + port + "/users/999",
                MessageResponse.class);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(UserMessagesEnum.USER_NOT_FOUND_MESSAGE.getMessage(),
                Objects.requireNonNull(responseEntity.getBody()).getMessage());
    }

    @Test
    @DisplayName("Получение информации о клиенте по номеру телефона: успешный сценарий")
    void getUserByPhoneNumber_Successfully() throws IOException {
        CreateUserRequest createUserRequest = readFromJson(
                "test-create-user-data.json",
                CreateUserRequest.class);

        userService.createUser(createUserRequest);

        ResponseEntity<GetUserResponse> responseEntity = restTemplate.getForEntity(
                BASE_HOST + port + "/users/phone-number/" + createUserRequest.getPhoneNumber(),
                GetUserResponse.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(1, Objects.requireNonNull(responseEntity.getBody()).getId());
        assertEquals(createUserRequest.getLastName(), Objects.requireNonNull(responseEntity.getBody()).getLastName());
        assertEquals(createUserRequest.getFirstName(), Objects.requireNonNull(responseEntity.getBody()).getFirstName());
        assertEquals(createUserRequest.getFatherName(), Objects.requireNonNull(responseEntity.getBody()).getFatherName());
        assertEquals(createUserRequest.getPhoneNumber(), Objects.requireNonNull(responseEntity.getBody()).getPhoneNumber());
        assertEquals(createUserRequest.getBirthDate(), Objects.requireNonNull(responseEntity.getBody()).getBirthDate());
        assertEquals(createUserRequest.getPassportNumber(), Objects.requireNonNull(responseEntity.getBody()).getPassportNumber());
        assertEquals(createUserRequest.getEmail(), responseEntity.getBody().getEmail());
        assertNotNull(responseEntity.getBody().getCreateDateTime());
        assertTrue(responseEntity.getBody().isActive());
        assertFalse(responseEntity.getBody().isDeleted());
    }

    @Test
    @DisplayName("Получение информации о клиенте по номеру телефона: ошибка UserNotFoundException")
    void getUserByPhoneNumber_UserNotFoundException() {
        ResponseEntity<MessageResponse> responseEntity = restTemplate.getForEntity(
                BASE_HOST + port + "/users/phone-number/70000000000",
                MessageResponse.class);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(UserMessagesEnum.USER_NOT_FOUND_MESSAGE.getMessage(),
                Objects.requireNonNull(responseEntity.getBody()).getMessage());
    }

    @Test
    @DisplayName("Изменение данных клиента: успешный сценарий")
    void updateUser_Successfully() throws IOException {
        CreateUserRequest createUserRequest = readFromJson(
                "test-create-user-data.json",
                CreateUserRequest.class);

        CreateUserResponse createUserResponse = userService.createUser(createUserRequest);

        UpdateUserRequest updateUserRequest = readFromJson(
                "test-update-user-data.json",
                UpdateUserRequest.class);

        ResponseEntity<UpdateUserResponse> responseEntity = restTemplate.exchange(
                BASE_HOST + port + "/users/" + createUserResponse.getClientId(),
                HttpMethod.PUT,
                new HttpEntity<>(updateUserRequest),
                UpdateUserResponse.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(UserMessagesEnum.USER_UPDATED_SUCCESSFULLY_MESSAGE.getMessage(),
                Objects.requireNonNull(responseEntity.getBody()).getMessage());
    }

    @Test
    @DisplayName("Изменение данных клиента: ошибка UserNotFoundException")
    void updateUser_UserNotFoundException() throws IOException {
        UpdateUserRequest updateUserRequest = readFromJson(
                "test-update-user-data.json",
                UpdateUserRequest.class);

        ResponseEntity<UpdateUserResponse> responseEntity = restTemplate.exchange(
                BASE_HOST + port + "/users/999",
                HttpMethod.PUT,
                new HttpEntity<>(updateUserRequest),
                UpdateUserResponse.class);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(UserMessagesEnum.USER_NOT_FOUND_MESSAGE.getMessage(),
                Objects.requireNonNull(responseEntity.getBody()).getMessage());
    }

    @Test
    @DisplayName("Удаление клиента: успешный сценарий")
    void deleteUser_Successfully() throws IOException {
        CreateUserRequest createUserRequest = readFromJson(
                "test-create-user-data.json",
                CreateUserRequest.class);

        CreateUserResponse createUserResponse = userService.createUser(createUserRequest);

        ResponseEntity<DeleteUserResponse> responseEntity = restTemplate.exchange(
                BASE_HOST + port + "/users/" + createUserResponse.getClientId(),
                HttpMethod.DELETE,
                null,
                DeleteUserResponse.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(UserMessagesEnum.USER_DELETED_SUCCESSFULLY_MESSAGE.getMessage(),
                Objects.requireNonNull(responseEntity.getBody()).getMessage());
    }

    @Test
    @DisplayName("Удаление клиента: ошибка UserNotFoundException")
    void deleteUser_UserNotFoundException() {
        ResponseEntity<DeleteUserResponse> responseEntity = restTemplate.exchange(
                BASE_HOST + port + "/users/1",
                HttpMethod.DELETE,
                null,
                DeleteUserResponse.class);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(UserMessagesEnum.USER_NOT_FOUND_MESSAGE.getMessage(),
                Objects.requireNonNull(responseEntity.getBody()).getMessage());
    }

    private <T> T readFromJson(String jsonFileName, Class<T> requestClass) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("json/" + jsonFileName);
        return objectMapper.readValue(inputStream, requestClass);
    }
}
