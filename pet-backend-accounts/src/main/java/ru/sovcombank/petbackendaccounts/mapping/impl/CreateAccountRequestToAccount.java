package ru.sovcombank.petbackendaccounts.mapping.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendaccounts.mapping.builder.Mapper;
import ru.sovcombank.petbackendaccounts.model.api.request.CreateAccountRequest;
import ru.sovcombank.petbackendaccounts.model.entity.Account;

@Component
public class CreateAccountRequestToAccount implements Mapper<CreateAccountRequest, Account> {

    private final ModelMapper modelMapper;

    public CreateAccountRequestToAccount(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public Account map(CreateAccountRequest createAccountRequest) {
        return modelMapper.map(createAccountRequest, Account.class);
    }
}
