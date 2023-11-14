package ru.sovcombank.petbackendusers.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateUserResponse {

    private String clientId;
    private String message;
}
