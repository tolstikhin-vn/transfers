package ru.sovcombank.petbackendtransfers.model.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sovcombank.petbackendtransfers.model.dto.AccountDTO;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetAccountsResponse {

    private List<AccountDTO> accountList;
}
