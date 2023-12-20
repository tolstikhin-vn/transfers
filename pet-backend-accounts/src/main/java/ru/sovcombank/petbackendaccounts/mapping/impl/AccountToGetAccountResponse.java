package ru.sovcombank.petbackendaccounts.mapping.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendaccounts.mapping.builder.Mapper;
import ru.sovcombank.petbackendaccounts.model.api.response.GetAccountResponse;
import ru.sovcombank.petbackendaccounts.model.entity.Account;

@Component
public class AccountToGetAccountResponse implements Mapper<Account, GetAccountResponse> {

    private final ModelMapper modelMapper;

    public AccountToGetAccountResponse(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public GetAccountResponse map(Account account) {
        return modelMapper.map(account, GetAccountResponse.class);
    }
}
