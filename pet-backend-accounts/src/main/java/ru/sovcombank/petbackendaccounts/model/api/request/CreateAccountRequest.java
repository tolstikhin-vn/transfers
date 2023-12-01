package ru.sovcombank.petbackendaccounts.model.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateAccountRequest {

    @NotNull
    private String clientId;

    @NotNull
    @Pattern(regexp = "^(810|840|933)$")
    private String cur;
}
