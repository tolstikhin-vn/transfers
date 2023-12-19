package ru.sovcombank.petbackendtransfers.mapping.impl;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendtransfers.mapping.builder.Mapper;
import ru.sovcombank.petbackendtransfers.model.api.response.GetBalanceResponse;

@Component
public class ResponseToGetBalanceResponse implements Mapper<ResponseEntity<Object>, GetBalanceResponse> {

    private final ModelMapper modelMapper;

    public ResponseToGetBalanceResponse(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    /**
     * Преобразует ответ с балансом счета из accounts в ответ с балансом счета.
     *
     * @param response Ответ с балансом счета из accounts.
     * @return Ответ с балансом счета.
     */
    @Override
    public GetBalanceResponse map(ResponseEntity<Object> response) {
        return modelMapper.map(response.getBody(), GetBalanceResponse.class);
    }
}
