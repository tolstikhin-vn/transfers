package ru.sovcombank.petbackendaccounts.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateAccountResponse {

    private String accountNumber;

    private String message;

    public CreateAccountResponse(String message) {
        this.message = message;
    }
}
