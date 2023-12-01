package ru.sovcombank.petbackendusers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.sovcombank.petbackendusers.exception.ConflictException;
import ru.sovcombank.petbackendusers.exception.UserNotFoundException;
import ru.sovcombank.petbackendusers.model.api.request.CreateUserRequest;
import ru.sovcombank.petbackendusers.model.api.request.UpdateUserRequest;
import ru.sovcombank.petbackendusers.model.api.response.CreateUserResponse;
import ru.sovcombank.petbackendusers.model.api.response.DeleteUserResponse;
import ru.sovcombank.petbackendusers.model.api.response.GetUserResponse;
import ru.sovcombank.petbackendusers.model.api.response.UpdateUserResponse;
import ru.sovcombank.petbackendusers.service.builder.UserService;

import java.io.IOException;
import java.io.InputStream;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserServiceITTest {

    @Container
    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest");

    @Autowired
    private UserService userService;

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

    @Test
    @DisplayName("Создание пользователя: успешный сценарий")
    void createUserSuccessTest() throws IOException {
        CreateUserRequest createUserRequest = readFromJson(
                "test-create-user-data.json",
                CreateUserRequest.class);

        CreateUserResponse createUserResponse = userService.createUser(createUserRequest);

        assertNotNull(createUserResponse);
        assertNotNull(createUserResponse.getClientId());
        assertEquals("Пользователь успешно создан", createUserResponse.getMessage());
    }

    @Test
    @DisplayName("Создание пользователя: ошибка из-за конфликта данных")
    void createUserWithDataIntegrityViolationExceptionTest() throws IOException {
        CreateUserRequest createUserRequest = readFromJson(
                "test-create-user-data.json",
                CreateUserRequest.class);

        userService.createUser(createUserRequest);

        assertThrows(ConflictException.class,
                () -> userService.createUser(createUserRequest));
    }

    @Test
    @DisplayName("Получение пользователя по идентификатору: успешный сценарий")
    void getUserByIdSuccessTest() throws IOException {
        CreateUserRequest createUserRequest = readFromJson(
                "test-create-user-data.json",
                CreateUserRequest.class);

        CreateUserResponse createUserResponse = userService.createUser(createUserRequest);

        GetUserResponse getUserResponse = userService.getUserById(
                createUserResponse.getClientId());

        assertNotNull(getUserResponse);
        assertEquals(createUserRequest.getLastName(), getUserResponse.getLastName());
        assertEquals(createUserRequest.getFirstName(), getUserResponse.getFirstName());
        assertEquals(createUserRequest.getFatherName(), getUserResponse.getFatherName());
        assertEquals(createUserRequest.getPhoneNumber(), getUserResponse.getPhoneNumber());
        assertEquals(createUserRequest.getBirthDate(), getUserResponse.getBirthDate());
        assertEquals(createUserRequest.getPassportNumber(), getUserResponse.getPassportNumber());
        assertEquals(createUserRequest.getEmail(), getUserResponse.getEmail());
        assertTrue(getUserResponse.isActive());
        assertFalse(getUserResponse.isDeleted());
    }

    @Test
    @DisplayName("Получение пользователя по несуществующему идентификатору: ошибка UserNotFoundException")
    void getUserByIdNotFoundTest() {
        String invalidClientId = "99999999";
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(invalidClientId));
        assertEquals("Не найден пользователь", exception.getMessage());
    }

    @Test
    @DisplayName("Получение пользователя по существующему номеру телефона: успешный сценарий")
    void getUserByPhoneNumberSuccessTest() throws IOException {
        CreateUserRequest createUserRequest = readFromJson(
                "test-create-user-data.json",
                CreateUserRequest.class);

        userService.createUser(createUserRequest);

        GetUserResponse getUserResponse = userService.getUserByPhoneNumber(
                createUserRequest.getPhoneNumber());

        assertNotNull(getUserResponse);
        assertEquals(createUserRequest.getLastName(), getUserResponse.getLastName());
        assertEquals(createUserRequest.getFirstName(), getUserResponse.getFirstName());
        assertEquals(createUserRequest.getFatherName(), getUserResponse.getFatherName());
        assertEquals(createUserRequest.getPhoneNumber(), getUserResponse.getPhoneNumber());
        assertEquals(createUserRequest.getBirthDate(), getUserResponse.getBirthDate());
        assertEquals(createUserRequest.getPassportNumber(), getUserResponse.getPassportNumber());
        assertEquals(createUserRequest.getEmail(), getUserResponse.getEmail());
        assertTrue(getUserResponse.isActive());
        assertFalse(getUserResponse.isDeleted());
    }

    @Test
    @DisplayName("Получение пользователя по несуществующему номеру телефона: ошибка UserNotFoundException")
    void getUserByPhoneNumberNotFoundTest() {
        String invalidPhoneNumber = "70000000000";
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.getUserByPhoneNumber(invalidPhoneNumber));
        assertEquals("Не найден пользователь", exception.getMessage());
    }

    @Test
    @DisplayName("Обновление данных пользователя: успешный сценарий")
    void updateUserSuccessTest() throws IOException {
        CreateUserRequest createUserRequest = readFromJson(
                "test-create-user-data.json",
                CreateUserRequest.class);

        CreateUserResponse createUserResponse = userService.createUser(createUserRequest);

        UpdateUserRequest updateUserRequest = readFromJson(
                "test-update-user-data.json",
                UpdateUserRequest.class);

        UpdateUserResponse updateUserResponse = userService.updateUser(createUserResponse.getClientId(), updateUserRequest);

        assertNotNull(updateUserResponse);
        assertEquals("Пользователь успешно изменен", updateUserResponse.getMessage());
    }

    @Test
    @DisplayName("Обновление пользователя: ошибка UserNotFoundException")
    void updateUserNotFoundTest() {
        String invalidClientId = "99999999";

        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setFirstName("NewFirstName");

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.updateUser(invalidClientId, updateUserRequest));
        assertEquals("Не найден пользователь", exception.getMessage());
    }

    @Test
    @DisplayName("Удаление пользователя: успешный сценарий")
    void deleteUserSuccessTest() throws IOException {
        CreateUserRequest createUserRequest = readFromJson(
                "test-create-user-data.json",
                CreateUserRequest.class);

        CreateUserResponse createUserResponse = userService.createUser(createUserRequest);

        DeleteUserResponse deleteUserResponse = userService.deleteUser(createUserResponse.getClientId());

        assertNotNull(deleteUserResponse);
        assertEquals("Пользователь успешно удален", deleteUserResponse.getMessage());
    }

    @Test
    @DisplayName("Удаление пользователя: ошибка UserNotFoundException")
    void deleteUserNotFoundTest() {
        String invalidClientId = "99999999";
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.deleteUser(invalidClientId));
        assertEquals("Не найден пользователь", exception.getMessage());
    }

    private <T> T readFromJson(String jsonFileName, Class<T> requestClass) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("json/" + jsonFileName);
        return objectMapper.readValue(inputStream, requestClass);
    }
}