package ru.sovcombank.petbackendusers.service;

import ru.sovcombank.petbackendusers.api.request.CreateUserRequest;
import ru.sovcombank.petbackendusers.api.request.UpdateUserRequest;
import ru.sovcombank.petbackendusers.api.response.CreateUserResponse;
import ru.sovcombank.petbackendusers.api.response.DeleteUserResponse;
import ru.sovcombank.petbackendusers.api.response.GetUserResponse;
import ru.sovcombank.petbackendusers.api.response.UpdateUserResponse;

public interface UserService {

    CreateUserResponse createUser(CreateUserRequest createUserRequest);
    GetUserResponse getUserById(String id);
    GetUserResponse getUserByPhoneNumber(String phoneNumber);
    UpdateUserResponse updateUser(String id, UpdateUserRequest updateUserRequest);
    DeleteUserResponse deleteUser(String id);
}
