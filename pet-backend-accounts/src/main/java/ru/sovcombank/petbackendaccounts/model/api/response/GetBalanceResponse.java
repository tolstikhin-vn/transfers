package ru.sovcombank.petbackendaccounts.model.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetBalanceResponse {

    private BigDecimal balance;
}
