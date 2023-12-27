package ru.sovcombank.petbackendusers.mapping.impl;

import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendusers.mapping.Mapper;
import ru.sovcombank.petbackendusers.model.api.request.UpdateUserRequest;
import ru.sovcombank.petbackendusers.model.entity.User;

import java.util.Optional;

@Component
public class UpdateUserRequestToUser implements Mapper<UpdateUserRequest, User> {

    /**
     * Преобразует запрос на изменение данных по клиенту в сущность пользователя.
     *
     * @param updateUserRequest Запрос на изменение данных по клиенту.
     * @return Сущность пользователя.
     */
    @Override
    public User map(UpdateUserRequest updateUserRequest) {
        User user = new User();
        user.setId(Integer.parseInt(updateUserRequest.getId()));
        Optional.ofNullable(updateUserRequest.getLastName()).ifPresent(user::setLastName);
        Optional.ofNullable(updateUserRequest.getFirstName()).ifPresent(user::setFirstName);
        Optional.ofNullable(updateUserRequest.getFatherName()).ifPresent(user::setFatherName);
        Optional.ofNullable(updateUserRequest.getPhoneNumber()).ifPresent(user::setPhoneNumber);
        Optional.ofNullable(updateUserRequest.getBirthDate()).ifPresent(user::setBirthDate);
        Optional.ofNullable(updateUserRequest.getPassportNumber()).ifPresent(user::setPassportNumber);
        Optional.ofNullable(updateUserRequest.getEmail()).ifPresent(user::setEmail);

        return user;
    }
}
