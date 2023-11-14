package ru.sovcombank.petbackendusers.dto;

import lombok.Data;

@Data
public class UserDTO {

    private String lastName;
    private String firstName;
    private String fatherName;
    private String phoneNumber;
    private String birthDate;
    private String passportNumber;
    private String email;
}
