package ru.sovcombank.petbackendtransfers.model.api.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MakeTransferByPhoneRequest {

    @NotNull
    private String clientId;

    @NotNull
    @Pattern(regexp = "^(ACCOUNT|PHONE)$")
    private String requestType;

    @NotNull
    private String phoneNumberFrom;

    @NotNull
    private String phoneNumberTo;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    @NotNull
    @Pattern(regexp = "^(810|840|933)$")
    private String cur;
}
