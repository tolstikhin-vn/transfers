package ru.sovcombank.petbackendtransfers.model.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetAccountResponse {

    private String accountNumber;

    private Integer clientId;

    private String cur;

    private BigDecimal balance;

    private boolean isMain;

    @JsonProperty(value = "isClosed")
    private boolean isClosed;
}
