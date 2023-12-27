package ru.sovcombank.petbackendaccounts.mapping.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendaccounts.mapping.Mapper;
import ru.sovcombank.petbackendaccounts.model.dto.AccountDTO;
import ru.sovcombank.petbackendaccounts.model.entity.Account;

@Component
public class AccountToAccountDTO implements Mapper<Account, AccountDTO> {

    private final ModelMapper modelMapper;

    public AccountToAccountDTO(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    /**
     * Преобразует сущность счета в DTO.
     *
     * @param account Сущность счета.
     * @return DTO счета.
     */
    @Override
    public AccountDTO map(Account account) {
        return modelMapper.map(account, AccountDTO.class);
    }
}
