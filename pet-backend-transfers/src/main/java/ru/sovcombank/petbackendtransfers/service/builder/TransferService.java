package ru.sovcombank.petbackendtransfers.service.builder;

import ru.sovcombank.petbackendtransfers.model.api.response.GetTransferResponse;
import ru.sovcombank.petbackendtransfers.model.api.response.MakeTransferResponse;

import java.util.Map;

public interface TransferService {

    MakeTransferResponse makeTransfer(Map<String, Object> requestMap);

    GetTransferResponse getTransfers(String uuid);
}
