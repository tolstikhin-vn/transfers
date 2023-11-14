package ru.sovcombank.petbackendusers.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.sovcombank.petbackendusers.api.request.CreateUserRequest;
import ru.sovcombank.petbackendusers.api.response.CreateUserResponse;
import ru.sovcombank.petbackendusers.api.response.GetUserResponse;
import ru.sovcombank.petbackendusers.exception.BadRequestException;
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
    private static final String ERROR_SAVING_TO_DATABASE_MESSAGE = "Ошибка при сохранении в базе данных";
    private static final String USER_NOT_FOUND_MESSAGE = "Не найден пользователь";
    private static final String BAD_REQUEST_MESSAGE_PREFIX = "Некорректный запрос по полю ";
    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "Internal server error";

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    /**
     * Создает нового пользователя на основе запроса.
     *
     * @param createUserRequest Запрос на создание пользователя.
     * @return Ответ с идентификатором созданного пользователя и сообщением об успешном создании.
     * @throws InternalServerErrorException В случае ошибки при сохранении в базе данных.
     */
    @Override
    public CreateUserResponse createUser(CreateUserRequest createUserRequest) {
        try {
            User user = userMapper.toEntity(createUserRequest);
            User createdUser = userRepository.save(user);
            return new CreateUserResponse(createdUser.getId().toString(), USER_CREATED_SUCCESSFULLY_MESSAGE);
        } catch (DataAccessException e) {
            throw new InternalServerErrorException(ERROR_SAVING_TO_DATABASE_MESSAGE, e);
        } catch (Exception e) {
            throw new InternalServerErrorException(INTERNAL_SERVER_ERROR_MESSAGE, e);
        }
    }

    /**
     * Получает информацию о пользователе по идентификатору.
     *
     * @param id Идентификатор пользователя.
     * @return Ответ с информацией о пользователе.
     * @throws UserNotFoundException        В случае, если пользователь не найден.
     * @throws BadRequestException          В случае некорректного запроса по полю id.
     * @throws InternalServerErrorException В случае внутренней ошибки сервера.
     */
    @Override
    public GetUserResponse getUserById(String id) {
        try {
            Optional<User> userOptional = userRepository.findById(Long.valueOf(id));
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                return userMapper.toGetUserResponse(user);
            } else {
                throw new UserNotFoundException(USER_NOT_FOUND_MESSAGE);
            }
        } catch (NumberFormatException e) {
            throw new BadRequestException(BAD_REQUEST_MESSAGE_PREFIX + id);
        } catch (Exception e) {
            throw new InternalServerErrorException(INTERNAL_SERVER_ERROR_MESSAGE, e);
        }
    }
}
