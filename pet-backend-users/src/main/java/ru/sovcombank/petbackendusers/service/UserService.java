package ru.sovcombank.petbackendusers.service;

import ru.sovcombank.petbackendusers.api.request.CreateUserRequest;
import ru.sovcombank.petbackendusers.api.response.CreateUserResponse;
import ru.sovcombank.petbackendusers.api.response.GetUserResponse;

public interface UserService {

    CreateUserResponse createUser(CreateUserRequest createUserRequest);
    GetUserResponse getUserById(String id);
//    GetUserResponse getUserByPhoneNumber(String phoneNumber);
}
