package ru.sovcombank.petbackendusers.model.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessageErrorResponse {

    private String message;
}
