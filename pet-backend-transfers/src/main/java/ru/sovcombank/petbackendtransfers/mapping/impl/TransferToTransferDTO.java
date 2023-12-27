package ru.sovcombank.petbackendtransfers.mapping.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendtransfers.mapping.Mapper;
import ru.sovcombank.petbackendtransfers.model.dto.TransferDTO;
import ru.sovcombank.petbackendtransfers.model.entity.Transfer;

@Component
public class TransferToTransferDTO implements Mapper<Transfer, TransferDTO> {

    private final ModelMapper modelMapper;

    public TransferToTransferDTO(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    /**
     * Преобразует сущность перевода в DTO перевода для history.
     *
     * @param transfer Сущность перевода.
     * @return Ответ DTO перевода.
     */
    @Override
    public TransferDTO map(Transfer transfer) {
        return modelMapper.map(transfer, TransferDTO.class);
    }
}