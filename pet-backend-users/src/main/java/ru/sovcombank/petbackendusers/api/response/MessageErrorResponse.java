package ru.sovcombank.petbackendusers.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessageErrorResponse {

    private String message;
}
