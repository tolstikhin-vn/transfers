package ru.sovcombank.petbackendtransfers.mapping.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendtransfers.mapping.Mapper;
import ru.sovcombank.petbackendtransfers.model.api.request.MakeTransferByAccountRequest;

import java.util.Map;

@Component
public class MapToMakeTransferByAccountRequest implements Mapper<Map<String, Object>, MakeTransferByAccountRequest> {

    private final ModelMapper modelMapper;

    public MapToMakeTransferByAccountRequest(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    /**
     * Преобразует запрос на создание перевода в запрос перевода по номеру счета.
     *
     * @param request Запрос на создание перевода.
     * @return Запрос перевода по номеру счета.
     */
    @Override
    public MakeTransferByAccountRequest map(Map<String, Object> request) {
        return modelMapper.map(request, MakeTransferByAccountRequest.class);
    }
}