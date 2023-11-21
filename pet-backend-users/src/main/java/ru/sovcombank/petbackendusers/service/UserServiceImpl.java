package ru.sovcombank.petbackendusers.service;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.sovcombank.petbackendusers.api.request.CreateUserRequest;
import ru.sovcombank.petbackendusers.api.request.UpdateUserRequest;
import ru.sovcombank.petbackendusers.api.response.CreateUserResponse;
import ru.sovcombank.petbackendusers.api.response.DeleteUserResponse;
import ru.sovcombank.petbackendusers.api.response.GetUserResponse;
import ru.sovcombank.petbackendusers.api.response.UpdateUserResponse;
import ru.sovcombank.petbackendusers.exception.ConflictException;
import ru.sovcombank.petbackendusers.exception.InternalServerErrorException;
import ru.sovcombank.petbackendusers.exception.UserNotFoundException;
import ru.sovcombank.petbackendusers.mapper.UserMapper;
import ru.sovcombank.petbackendusers.model.User;
import ru.sovcombank.petbackendusers.repository.UserRepository;

import java.util.Optional;

/**
 * Реализация сервиса пользователей.
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private static final String USER_CREATED_SUCCESSFULLY_MESSAGE = "Пользователь успешно создан";
    private static final String USER_UPDATED_SUCCESSFULLY_MESSAGE = "Пользователь успешно изменен";
    private static final String USER_DELETED_SUCCESSFULLY_MESSAGE = "Пользователь успешно удален";
    private static final String USER_NOT_FOUND_MESSAGE = "Не найден пользователь";

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    /**
     * Создает нового клиента на основе запроса.
     *
     * @param createUserRequest Запрос на создание клиента.
     * @return Ответ с идентификатором созданного клиента и сообщением об успешном создании.
     * @throws InternalServerErrorException В случае ошибки при сохранении в базе данных.
     */
    @Override
    public CreateUserResponse createUser(CreateUserRequest createUserRequest) {
        try {
            User createdUser = userRepository.save(userMapper.toEntity(createUserRequest));
            return new CreateUserResponse(createdUser.getId().toString(), USER_CREATED_SUCCESSFULLY_MESSAGE);
        } catch (DataIntegrityViolationException ex) {
            if (ex.getCause() instanceof ConstraintViolationException) {
                throw new ConflictException(ex);
            }
            throw new InternalServerErrorException(ex);
        }
    }

    /**
     * Получает информацию о пользователе по идентификатору.
     *
     * @param id Идентификатор клиента.
     * @return Ответ с информацией о пользователе.
     * @throws UserNotFoundException        В случае, если пользователь не найден.
     * @throws InternalServerErrorException В случае внутренней ошибки сервера.
     */
    @Override
    public GetUserResponse getUserById(String id) {

        Optional<User> userOptional = userRepository.findById(Long.valueOf(id));
        if (userOptional.isPresent()) {
            return userMapper.toGetUserResponse(userOptional.get());
        } else {
            throw new UserNotFoundException(USER_NOT_FOUND_MESSAGE);
        }
    }

    /**
     * Получает информацию о пользователе по номеру телефона.
     *
     * @param phoneNumber Идентификатор клиента.
     * @return Ответ с информацией о пользователе.
     * @throws UserNotFoundException        В случае, если пользователь не найден.
     * @throws InternalServerErrorException В случае внутренней ошибки сервера.
     */
    @Override
    public GetUserResponse getUserByPhoneNumber(String phoneNumber) {
        Optional<User> userOptional = userRepository.findByPhoneNumber(phoneNumber);
        if (userOptional.isPresent()) {
            return userMapper.toGetUserResponse(userOptional.get());
        } else {
            throw new UserNotFoundException(USER_NOT_FOUND_MESSAGE);
        }
    }

    /**
     * Изменяет данные по клиенту.
     *
     * @param id                Идентификатор клиента.
     * @param updateUserRequest Запрос на изменение данных.
     * @return Ответ с сообщением.
     * @throws UserNotFoundException        В случае, если пользователь не найден.
     * @throws InternalServerErrorException В случае внутренней ошибки сервера.
     */
    @Override
    public UpdateUserResponse updateUser(String id, UpdateUserRequest updateUserRequest) {
        try {
            // Проверяем существование клиента по переданному идентификатору
            Optional<User> userOptional = userRepository.findById(Long.valueOf(id));
            if (userOptional.isPresent()) {
                userRepository.save(userMapper.toEntity(updateUserRequest, userOptional.get()));
                return new UpdateUserResponse(USER_UPDATED_SUCCESSFULLY_MESSAGE);
            } else {
                throw new UserNotFoundException(USER_NOT_FOUND_MESSAGE);
            }
        } catch (DataIntegrityViolationException ex) {
            if (ex.getCause() instanceof ConstraintViolationException) {
                throw new ConflictException(ex);
            }
            throw new InternalServerErrorException(ex);
        }
    }

    /**
     * Удаляет клиента (изменение поля isDeleted на true).
     *
     * @param id Идентификатор клиента.
     * @return Ответ с сообщением.
     * @throws UserNotFoundException        В случае, если пользователь не найден.
     * @throws InternalServerErrorException В случае внутренней ошибки сервера.
     */
    @Override
    public DeleteUserResponse deleteUser(String id) {
        // Проверяем существование клиента по переданному идентификатору
        Optional<User> userOptional = userRepository.findById(Long.valueOf(id));
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setIsDeleted(true);
            userRepository.save(user);
            return new DeleteUserResponse(USER_DELETED_SUCCESSFULLY_MESSAGE);
        } else {
            throw new UserNotFoundException(USER_NOT_FOUND_MESSAGE);
        }
    }
}
