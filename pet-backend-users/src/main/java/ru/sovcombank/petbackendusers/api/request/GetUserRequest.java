package ru.sovcombank.petbackendusers.api.request;

import lombok.Data;

@Data
public class GetUserRequest {

    private String lastName;
    private String firstName;
    private String fatherName;
    private String phoneNumber;
    private String birthDate;
    private String passportNumber;
    private String email;
    private boolean isActive;
}
