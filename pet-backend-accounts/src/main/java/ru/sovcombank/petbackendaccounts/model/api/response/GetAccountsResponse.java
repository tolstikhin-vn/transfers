package ru.sovcombank.petbackendaccounts.model.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sovcombank.petbackendaccounts.model.dto.AccountDTO;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAccountsResponse {

    private Integer clientId;

    private List<AccountDTO> accountNumbers;
}
