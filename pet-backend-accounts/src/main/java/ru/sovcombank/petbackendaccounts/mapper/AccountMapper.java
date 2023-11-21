package ru.sovcombank.petbackendaccounts.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendaccounts.api.request.CreateAccountRequest;
import ru.sovcombank.petbackendaccounts.model.Account;

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
}
