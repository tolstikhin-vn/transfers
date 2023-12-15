package ru.sovcombank.petbackendhistory.model.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sovcombank.petbackendhistory.model.dto.AccountDTO;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAccountsResponse {

    private String clientId;

    private List<AccountDTO> accountList;
}