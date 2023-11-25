package ru.sovcombank.petbackendaccounts.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountDTO {

    private String accountNumber;
    private String cur;

    @JsonProperty(value = "isMain")
    private boolean isMain;
}
