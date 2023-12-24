package ru.sovcombank.petbackendusers.model.api.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {

    private String id;
    private String lastName;
    private String firstName;
    private String fatherName;
    @Pattern(regexp = "^7[0-9]{10}$")
    private String phoneNumber;
    @Pattern(regexp = "^(0[1-9]|[12][0-9]|3[01])\\.(0[1-9]|1[0-2])\\.\\d{4}$")
    private String birthDate;
    @Size(min = 10, max = 10)
    private String passportNumber;
    @Email
    private String email;
    private boolean isActive;
}
