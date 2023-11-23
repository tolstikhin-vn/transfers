package ru.sovcombank.petbackendaccounts.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendaccounts.api.request.CreateAccountRequest;
import ru.sovcombank.petbackendaccounts.api.response.GetAccountsResponse;
import ru.sovcombank.petbackendaccounts.dto.AccountDTO;
import ru.sovcombank.petbackendaccounts.model.Account;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AccountMapper {

    private final ModelMapper modelMapper;

    @Autowired
    public AccountMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    /**
     * Преобразует запрос на создание счета в сущность счета.
     *
     * @param createAccountRequest Запрос на создание счета.
     * @return Сущность счета.
     */
    public Account toEntity(CreateAccountRequest createAccountRequest) {
        return modelMapper.map(createAccountRequest, Account.class);
    }

    /**
     * Преобразует список сущностей счетов в GetAccountsResponse.
     *
     * @param accounts Список сущностей счетов.
     * @param clientId ID клиента.
     * @return ответ GetAccountsResponse с информацией о счетах.
     */
    public GetAccountsResponse toGetAccountsResponse(List<Account> accounts, String clientId) {
        List<AccountDTO> accountDtos = accounts.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        GetAccountsResponse response = new GetAccountsResponse();
        response.setClientId(clientId);
        response.setAccountNumbers(accountDtos);

        return response;
    }

    /**
     * Преобразует сущность счета в DTO.
     *
     * @param account Сущность счета.
     * @return DTO счета.
     */
    public AccountDTO toDTO(Account account) {
        return modelMapper.map(account, AccountDTO.class);
    }
}
