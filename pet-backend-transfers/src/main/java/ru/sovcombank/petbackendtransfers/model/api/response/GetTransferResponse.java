package ru.sovcombank.petbackendtransfers.model.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetTransferResponse {

    private String accountNumberFrom;

    private String accountNumberTo;

    private BigDecimal amount;

    private String cur;

    private LocalDateTime transactionDateTime;
}
