package ru.sovcombank.petbackendusers.mapping.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendusers.mapping.builder.Mapper;
import ru.sovcombank.petbackendusers.model.api.response.GetUserResponse;
import ru.sovcombank.petbackendusers.model.entity.User;

@Component
public class UserToGetUserResponse implements Mapper<User, GetUserResponse> {

    private final ModelMapper modelMapper;

    public UserToGetUserResponse(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    /**
     * Преобразует сущность пользователя в ответ на запрос получения информации о пользователе.
     *
     * @param user Сущность пользователя.
     * @return Ответ на запрос получения информации о пользователе.
     */
    @Override
    public GetUserResponse map(User user) {
        return modelMapper.map(user, GetUserResponse.class);
    }
}
