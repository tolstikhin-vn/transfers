package ru.sovcombank.petbackendusers.controller;

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
import ru.sovcombank.petbackendusers.model.api.request.CreateUserRequest;
import ru.sovcombank.petbackendusers.model.api.request.UpdateUserRequest;
import ru.sovcombank.petbackendusers.model.api.response.CreateUserResponse;
import ru.sovcombank.petbackendusers.model.api.response.DeleteUserResponse;
import ru.sovcombank.petbackendusers.model.api.response.GetUserResponse;
import ru.sovcombank.petbackendusers.model.api.response.UpdateUserResponse;
import ru.sovcombank.petbackendusers.service.UserService;

/**
 * Контроллер для управления пользователями.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Обрабатывает запрос на создание нового клиента.
     *
     * @param createUserRequest Запрос на создание клиента.
     * @return Ответ с результатом создания клиента.
     */
    @PostMapping()
    public ResponseEntity<Object> createUser(@Valid @RequestBody CreateUserRequest createUserRequest) {
        CreateUserResponse response = userService.createUser(createUserRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Обрабатывает запрос на получение информации о пользователе по идентификатору.
     *
     * @param id Идентификатор клиента.
     * @return Ответ с информацией о пользователе.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable String id) {
        GetUserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Обрабатывает запрос на получение информации о пользователе по номеру телефона.
     *
     * @param phoneNumber Номер телефона клиента.
     * @return Ответ с информацией о пользователе.
     */
    @GetMapping("/phone-number/{phoneNumber}")
    public ResponseEntity<Object> getUserByPhoneNumber(@PathVariable String phoneNumber) {
        GetUserResponse response = userService.getUserByPhoneNumber(phoneNumber);
        return ResponseEntity.ok(response);
    }

    /**
     * Обрабатывает запрос на изменение данных по клиенту.
     *
     * @param id                Идентификатор клиента.
     * @param updateUserRequest Запрос на изменение данных по клиенту.
     * @return Ответ с результатом изменения данных по клиенту.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable String id, @Valid @RequestBody UpdateUserRequest updateUserRequest) {
        UpdateUserResponse response = userService.updateUser(id, updateUserRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Обрабатывает запрос на удаление клиента.
     *
     * @param id Идентификатор клиента.
     * @return Ответ с результатом удаления клиента.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable String id) {
        DeleteUserResponse response = userService.deleteUser(id);
        return ResponseEntity.ok(response);
    }
}
