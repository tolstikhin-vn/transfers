package ru.sovcombank.petbackendtransfers.mapping.impl;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendtransfers.mapping.Mapper;
import ru.sovcombank.petbackendtransfers.model.api.response.GetAccountsResponse;

@Component
public class ResponseToGetAccountsResponse implements Mapper<ResponseEntity<Object>, GetAccountsResponse> {

    private final ModelMapper modelMapper;

    public ResponseToGetAccountsResponse(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    /**
     * Преобразует ответ со счетами из accounts в ответ с информацией о счетах.
     *
     * @param response Ответ со счетами из accounts.
     * @return Ответ с информацией о счетах.
     */
    @Override
    public GetAccountsResponse map(ResponseEntity<Object> response) {
        return modelMapper.map(response.getBody(), GetAccountsResponse.class);
    }
}
