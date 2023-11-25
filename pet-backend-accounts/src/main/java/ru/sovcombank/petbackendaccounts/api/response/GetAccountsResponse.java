package ru.sovcombank.petbackendaccounts.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sovcombank.petbackendaccounts.dto.AccountDTO;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetAccountsResponse {

    private String clientId;

    private List<AccountDTO> accountNumbers;
}
