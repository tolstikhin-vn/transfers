package ru.sovcombank.petbackendusers.service.impl;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sovcombank.petbackendusers.builder.ResponseBuilder;
import ru.sovcombank.petbackendusers.exception.ConflictException;
import ru.sovcombank.petbackendusers.exception.InternalServerErrorException;
import ru.sovcombank.petbackendusers.exception.UserNotFoundException;
import ru.sovcombank.petbackendusers.mapping.impl.CreateUserRequestToUser;
import ru.sovcombank.petbackendusers.mapping.impl.UpdateUserRequestToUser;
import ru.sovcombank.petbackendusers.mapping.impl.UserToGetUserResponse;
import ru.sovcombank.petbackendusers.model.api.request.CreateUserRequest;
import ru.sovcombank.petbackendusers.model.api.request.UpdateUserRequest;
import ru.sovcombank.petbackendusers.model.api.response.CreateUserResponse;
import ru.sovcombank.petbackendusers.model.api.response.DeleteUserResponse;
import ru.sovcombank.petbackendusers.model.api.response.GetUserResponse;
import ru.sovcombank.petbackendusers.model.api.response.UpdateUserResponse;
import ru.sovcombank.petbackendusers.model.entity.User;
import ru.sovcombank.petbackendusers.model.enums.UserMessagesEnum;
import ru.sovcombank.petbackendusers.repository.UserRepository;
import ru.sovcombank.petbackendusers.service.builder.UserService;

import java.util.Optional;

/**
 * Реализация сервиса пользователей.
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CreateUserRequestToUser createUserRequestToUser;
    private final UpdateUserRequestToUser updateUserRequestToUser;
    private final UserToGetUserResponse userToGetUserResponse;
    private final ResponseBuilder responseBuilder;

    public UserServiceImpl(UserRepository userRepository,
                           CreateUserRequestToUser createUserRequestToUser,
                           UpdateUserRequestToUser updateUserRequestToUser,
                           UserToGetUserResponse userToGetUserResponse,
                           ResponseBuilder responseBuilder) {
        this.userRepository = userRepository;
        this.createUserRequestToUser = createUserRequestToUser;
        this.updateUserRequestToUser = updateUserRequestToUser;
        this.userToGetUserResponse = userToGetUserResponse;
        this.responseBuilder = responseBuilder;
    }

    /**
     * Создает нового клиента на основе запроса.
     *
     * @param createUserRequest Запрос на создание клиента.
     * @return Ответ с идентификатором созданного клиента и сообщением об успешном создании.
     * @throws InternalServerErrorException В случае ошибки при сохранении в базе данных.
     */
    @Override
    @Transactional
    public CreateUserResponse createUser(CreateUserRequest createUserRequest) {
        try {
            User createdUser = userRepository.save(createUserRequestToUser.map(createUserRequest));
            return responseBuilder.buildCreateUserResponse(createdUser.getId());
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
     * @throws UserNotFoundException В случае, если пользователь не найден.
     */
    @Override
    public GetUserResponse getUserById(String id) {
        Optional<User> userOptional = userRepository.findById(Long.valueOf(id));
        if (userOptional.isPresent()) {
            return userToGetUserResponse.map(userOptional.get());
        } else {
            throw new UserNotFoundException(UserMessagesEnum.USER_NOT_FOUND_MESSAGE.getMessage());
        }
    }

    /**
     * Получает информацию о пользователе по номеру телефона.
     *
     * @param phoneNumber Идентификатор клиента.
     * @return Ответ с информацией о пользователе.
     * @throws UserNotFoundException В случае, если пользователь не найден.
     */
    @Override
    public GetUserResponse getUserByPhoneNumber(String phoneNumber) {
        Optional<User> userOptional = userRepository.findByPhoneNumber(phoneNumber);
        if (userOptional.isPresent()) {
            return userToGetUserResponse.map(userOptional.get());
        } else {
            throw new UserNotFoundException(UserMessagesEnum.USER_NOT_FOUND_MESSAGE.getMessage());
        }
    }

    /**
     * Изменяет данные по клиенту.
     *
     * @param id                Идентификатор клиента.
     * @param updateUserRequest Запрос на изменение данных.
     * @return Ответ с сообщением.
     * @throws UserNotFoundException В случае, если пользователь не найден.
     */
    @Override
    @Transactional
    public UpdateUserResponse updateUser(String id, UpdateUserRequest updateUserRequest) {
        try {
            // Проверяем существование клиента по переданному идентификатору
            Optional<User> userOptional = userRepository.findById(Long.valueOf(id));
            if (userOptional.isPresent()) {
                updateUserRequest.setId(id);
                userRepository.save(updateUserRequestToUser.map(updateUserRequest));

                return new UpdateUserResponse(UserMessagesEnum.USER_UPDATED_SUCCESSFULLY_MESSAGE.getMessage());
            } else {
                throw new UserNotFoundException(UserMessagesEnum.USER_NOT_FOUND_MESSAGE.getMessage());
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
     * @throws UserNotFoundException В случае, если пользователь не найден.
     */
    @Override
    @Transactional
    public DeleteUserResponse deleteUser(String id) {
        // Проверяем существование клиента по переданному идентификатору
        Optional<User> userOptional = userRepository.findById(Long.valueOf(id));
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setIsDeleted(true);
            userRepository.save(user);

            return new DeleteUserResponse(UserMessagesEnum.USER_DELETED_SUCCESSFULLY_MESSAGE.getMessage());
        } else {
            throw new UserNotFoundException(UserMessagesEnum.USER_NOT_FOUND_MESSAGE.getMessage());
        }
    }
}
