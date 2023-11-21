package ru.sovcombank.petbackendaccounts.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateAccountResponse {

    private String accountNumber;

    private String message;
}
