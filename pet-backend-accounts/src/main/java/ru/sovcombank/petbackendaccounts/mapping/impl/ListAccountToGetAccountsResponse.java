package ru.sovcombank.petbackendaccounts.mapping.impl;

import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendaccounts.mapping.Mapper;
import ru.sovcombank.petbackendaccounts.model.api.response.GetAccountsResponse;
import ru.sovcombank.petbackendaccounts.model.dto.AccountDTO;
import ru.sovcombank.petbackendaccounts.model.entity.Account;

import java.util.List;

@Component
public class ListAccountToGetAccountsResponse implements Mapper<List<Account>, GetAccountsResponse> {

    private final Mapper<Account, AccountDTO> accountToAccountDTO;
    private Integer clientId;

    public ListAccountToGetAccountsResponse(AccountToAccountDTO accountToAccountDTO) {
        this.accountToAccountDTO = accountToAccountDTO;
    }

    /**
     * Преобразует список сущностей счетов в GetAccountsResponse.
     *
     * @param accounts Список сущностей счетов.
     * @return ответ GetAccountsResponse с информацией о счетах.
     */
    @Override
    public GetAccountsResponse map(List<Account> accounts) {
        List<AccountDTO> accountDtos = accounts.stream()
                .map(accountToAccountDTO::map)
                .toList();

        GetAccountsResponse response = new GetAccountsResponse();
        response.setClientId(clientId);
        response.setAccountNumbers(accountDtos);

        return response;
    }
}
