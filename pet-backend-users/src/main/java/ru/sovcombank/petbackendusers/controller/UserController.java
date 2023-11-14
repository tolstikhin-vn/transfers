package ru.sovcombank.petbackendusers.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sovcombank.petbackendusers.api.request.CreateUserRequest;
import ru.sovcombank.petbackendusers.api.response.CreateUserResponse;
import ru.sovcombank.petbackendusers.api.response.GetUserResponse;
import ru.sovcombank.petbackendusers.service.UserService;

/**
 * Контроллер для управления пользователями.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Обрабатывает запрос на создание нового пользователя.
     *
     * @param createUserRequest Запрос на создание пользователя.
     * @return Ответ с результатом создания пользователя.
     */
    @PostMapping("/new")
    public ResponseEntity<Object> createUser(@Valid @RequestBody CreateUserRequest createUserRequest) {
        CreateUserResponse response = userService.createUser(createUserRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Обрабатывает запрос на получение информации о пользователе по идентификатору.
     *
     * @param id Идентификатор пользователя.
     * @return Ответ с информацией о пользователе.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable String id) {
        GetUserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

//    @GetMapping("/{phoneNumber}")
//    public ResponseEntity<Object> getUserByPhoneNumber(@PathVariable String phoneNumber) {
//        GetUserResponse response = userService.getUserByPhoneNumber(phoneNumber);
//        return ResponseEntity.ok(response);
//    }
}
