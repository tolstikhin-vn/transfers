package ru.sovcombank.petbackendusers.service;

import ru.sovcombank.petbackendusers.model.api.request.CreateUserRequest;
import ru.sovcombank.petbackendusers.model.api.request.UpdateUserRequest;
import ru.sovcombank.petbackendusers.model.api.response.CreateUserResponse;
import ru.sovcombank.petbackendusers.model.api.response.DeleteUserResponse;
import ru.sovcombank.petbackendusers.model.api.response.GetUserResponse;
import ru.sovcombank.petbackendusers.model.api.response.UpdateUserResponse;

public interface UserService {

    CreateUserResponse createUser(CreateUserRequest createUserRequest);
    GetUserResponse getUserById(String id);
    GetUserResponse getUserByPhoneNumber(String phoneNumber);
    UpdateUserResponse updateUser(String id, UpdateUserRequest updateUserRequest);
    DeleteUserResponse deleteUser(String id);
}
