package ru.sovcombank.petbackendaccounts.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateAccountRequest {

    @NotNull
    private String clientId;

    @NotNull
    @Pattern(regexp = "^(810|840|933)$")
    private String cur;
}
