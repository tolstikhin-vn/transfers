package ru.sovcombank.petbackendtransfers.model.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetAccountResponse {

    private String accountNumber;
    private String clientId;
    private String cur;
    private BigDecimal balance;
    private boolean isMain;
    private boolean isClosed;
}
