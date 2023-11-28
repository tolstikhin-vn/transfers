package ru.sovcombank.petbackendaccounts.model.api.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class UpdateBalanceRequest {

    @NotNull
    String typePayments;
    @NotNull
    @DecimalMin(value = "0.01")
    BigDecimal amount;
}
