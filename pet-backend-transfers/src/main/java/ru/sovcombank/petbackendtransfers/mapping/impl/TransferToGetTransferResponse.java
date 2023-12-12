package ru.sovcombank.petbackendtransfers.mapping.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendtransfers.mapping.builder.Mapper;
import ru.sovcombank.petbackendtransfers.model.api.response.GetTransferResponse;
import ru.sovcombank.petbackendtransfers.model.entity.Transfer;

@Component
public class TransferToGetTransferResponse implements Mapper<Transfer, GetTransferResponse> {

    private final ModelMapper modelMapper;

    public TransferToGetTransferResponse(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    /**
     * Преобразует сущность перевода в ответ с информацией о переводе.
     *
     * @param transfer Сущность перевода.
     * @return Ответ с информацией о переводе.
     */
    @Override
    public GetTransferResponse map(Transfer transfer) {
        return modelMapper.map(transfer, GetTransferResponse.class);
    }
}