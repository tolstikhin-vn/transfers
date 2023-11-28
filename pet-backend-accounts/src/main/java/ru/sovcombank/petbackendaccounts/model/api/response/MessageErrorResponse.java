package ru.sovcombank.petbackendaccounts.model.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageErrorResponse {

    private String message;
}
