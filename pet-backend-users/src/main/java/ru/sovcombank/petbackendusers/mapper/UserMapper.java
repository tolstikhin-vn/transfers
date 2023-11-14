package ru.sovcombank.petbackendusers.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendusers.api.request.CreateUserRequest;
import ru.sovcombank.petbackendusers.api.response.CreateUserResponse;
import ru.sovcombank.petbackendusers.api.response.GetUserResponse;
import ru.sovcombank.petbackendusers.dto.UserDTO;
import ru.sovcombank.petbackendusers.model.User;

/**
 * Класс-маппер для преобразования между сущностями и DTO пользователей.
 */
@Component
public class UserMapper {

    private final ModelMapper modelMapper;

    /**
     * Конструктор класса UserMapper.
     *
     * @param modelMapper Экземпляр ModelMapper.
     */
    @Autowired
    public UserMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    /**
     * Преобразует сущность пользователя в DTO.
     *
     * @param user Сущность пользователя.
     * @return DTO пользователя.
     */
    public UserDTO toDto(User user) {
        return modelMapper.map(user, UserDTO.class);
    }

    /**
     * Преобразует DTO пользователя в сущность.
     *
     * @param userDTO DTO пользователя.
     * @return Сущность пользователя.
     */
    public User toEntity(UserDTO userDTO) {
        return modelMapper.map(userDTO, User.class);
    }

    /**
     * Преобразует запрос на создание пользователя в сущность пользователя.
     *
     * @param createUserRequest Запрос на создание пользователя.
     * @return Сущность пользователя.
     */
    public User toEntity(CreateUserRequest createUserRequest) {
        return modelMapper.map(createUserRequest, User.class);
    }

    /**
     * Преобразует DTO пользователя в ответ на запрос создания пользователя.
     *
     * @param userDTO        DTO пользователя.
     * @param responseClass  Класс ответа на запрос создания пользователя.
     * @return Ответ на запрос создания пользователя.
     */
    public CreateUserResponse toDto(UserDTO userDTO, Class<CreateUserResponse> responseClass) {
        return modelMapper.map(userDTO, responseClass);
    }

    /**
     * Преобразует сущность пользователя в ответ на запрос получения информации о пользователе.
     *
     * @param user Сущность пользователя.
     * @return Ответ на запрос получения информации о пользователе.
     */
    public GetUserResponse toGetUserResponse(User user) {
        return modelMapper.map(user, GetUserResponse.class);
    }
}
