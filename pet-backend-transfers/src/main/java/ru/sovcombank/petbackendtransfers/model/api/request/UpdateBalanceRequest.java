package ru.sovcombank.petbackendtransfers.model.api.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBalanceRequest {

    @NotNull
    String typePayments;
    @NotNull
    @DecimalMin(value = "0.01")
    BigDecimal amount;
}

