package ru.sovcombank.petbackendusers.model.api.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createDateTime;
    @JsonProperty(value = "isActive")
    private boolean isActive;
    @JsonProperty(value = "isDeleted")
    private boolean isDeleted;
}