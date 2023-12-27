package ru.sovcombank.petbackendtransfers.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferDTO {

    private UUID uuid;

    private Integer clientIdFrom;

    private Integer clientIdTo;

    private String accountNumberFrom;

    private String accountNumberTo;

    private BigDecimal amount;

    private String cur;

    private LocalDateTime transactionDateTime;
}
