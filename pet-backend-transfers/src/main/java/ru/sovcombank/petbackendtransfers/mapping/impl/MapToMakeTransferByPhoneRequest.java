package ru.sovcombank.petbackendtransfers.mapping.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendtransfers.mapping.Mapper;
import ru.sovcombank.petbackendtransfers.model.api.request.MakeTransferByPhoneRequest;

import java.util.Map;

@Component
public class MapToMakeTransferByPhoneRequest implements Mapper<Map<String, Object>, MakeTransferByPhoneRequest> {


    private final ModelMapper modelMapper;

    public MapToMakeTransferByPhoneRequest(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    /**
     * Преобразует запрос на создание перевода в запрос перевода по номеру телефона.
     *
     * @param request Запрос на создание перевода.
     * @return Запрос перевода по номеру телефона.
     */
    @Override
    public MakeTransferByPhoneRequest map(Map<String, Object> request) {
        return modelMapper.map(request, MakeTransferByPhoneRequest.class);
    }
}
