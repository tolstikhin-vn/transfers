package ru.sovcombank.petbackendaccounts.model.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetAccountResponse {

    private int id;
    private String accountNumber;
    private String clientId;
    private String cur;
    private BigDecimal balance;
    private LocalDateTime createDateTime;
    private boolean isMain;
    private boolean isClosed;
}
