package ru.sovcombank.petbackendaccounts.mapping.impl;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendaccounts.mapping.Mapper;
import ru.sovcombank.petbackendaccounts.model.api.response.GetUserResponse;

@Component
public class ResponseToGetUserResponse implements Mapper<ResponseEntity<Object>, GetUserResponse> {

    private final ModelMapper modelMapper;

    public ResponseToGetUserResponse(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    /**
     * Преобразует ответ с информацией о клиенте из users в ответ с информацией о клиенте.
     *
     * @param response Ответ с информацией о клиенте из users.
     * @return Ответ с информацией о клиенте.
     */
    @Override
    public GetUserResponse map(ResponseEntity<Object> response) {
        return modelMapper.map(response.getBody(), GetUserResponse.class);
    }
}

