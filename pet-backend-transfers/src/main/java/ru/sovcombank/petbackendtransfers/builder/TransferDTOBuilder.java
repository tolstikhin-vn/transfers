package ru.sovcombank.petbackendtransfers.builder;

import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendtransfers.mapping.impl.TransferToTransferDTO;
import ru.sovcombank.petbackendtransfers.model.dto.TransferDTO;
import ru.sovcombank.petbackendtransfers.model.entity.Transfer;

@Component
public class TransferDTOBuilder {

    private final TransferToTransferDTO transferToTransferDTO;

    public TransferDTOBuilder(TransferToTransferDTO transferToTransferDTO) {
        this.transferToTransferDTO = transferToTransferDTO;
    }

    // Создание объекта TransferDTO по значениям полей
    public TransferDTO createTransferDTOObject(Transfer transfer, Integer clientIdFrom, Integer clientIdTo) {
        TransferDTO transferDTO = transferToTransferDTO.map(transfer);
        transferDTO.setClientIdFrom(clientIdFrom);
        transferDTO.setClientIdTo(clientIdTo);
        return transferDTO;
    }
}
