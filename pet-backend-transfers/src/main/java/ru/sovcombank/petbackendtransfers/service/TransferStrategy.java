package ru.sovcombank.petbackendtransfers.service;

import ru.sovcombank.petbackendtransfers.model.api.response.MakeTransferResponse;

import java.util.Map;

public interface TransferStrategy {
    MakeTransferResponse makeTransfer(Map<String, Object> requestMap);
}