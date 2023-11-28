package ru.sovcombank.petbackendusers.mapping.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendusers.mapping.builder.Mapper;
import ru.sovcombank.petbackendusers.model.api.request.CreateUserRequest;
import ru.sovcombank.petbackendusers.model.entity.User;

@Component
public class CreateUserRequestToUser implements Mapper<CreateUserRequest, User> {

    private final ModelMapper modelMapper;

    public CreateUserRequestToUser(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    /**
     * Преобразует запрос на создание пользователя в сущность пользователя.
     *
     * @param createUserRequest Запрос на создание пользователя.
     * @return Сущность пользователя.
     */
    @Override
    public User map(CreateUserRequest createUserRequest) {
        return modelMapper.map(createUserRequest, User.class);
    }
}
