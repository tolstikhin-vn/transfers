package ru.sovcombank.petbackendusers.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class GetUserResponse {

    private int id;
    private String lastName;
    private String firstName;
    private String fatherName;
    private String phoneNumber;
    private String birthDate;
    private String passportNumber;
    private String email;
    private LocalDateTime createDateTime;
    private boolean isActive;
    private boolean isDeleted;
}