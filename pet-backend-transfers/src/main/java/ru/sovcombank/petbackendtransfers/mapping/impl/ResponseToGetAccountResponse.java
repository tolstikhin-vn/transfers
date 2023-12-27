package ru.sovcombank.petbackendtransfers.mapping.impl;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendtransfers.mapping.Mapper;
import ru.sovcombank.petbackendtransfers.model.api.response.GetAccountResponse;

@Component
public class ResponseToGetAccountResponse implements Mapper<ResponseEntity<Object>, GetAccountResponse> {

    private final ModelMapper modelMapper;

    public ResponseToGetAccountResponse(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    /**
     * Преобразует ответ со счетом из accounts в ответ с информацией о счете.
     *
     * @param response Ответ со счетом из accounts.
     * @return Ответ с информацией о счете.
     */
    @Override
    public GetAccountResponse map(ResponseEntity<Object> response) {
        return modelMapper.map(response.getBody(), GetAccountResponse.class);
    }
}