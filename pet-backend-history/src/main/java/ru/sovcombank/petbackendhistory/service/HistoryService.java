package ru.sovcombank.petbackendhistory.service;

import ru.sovcombank.petbackendhistory.model.api.response.GetTransferHistoryResponse;

public interface HistoryService {

    GetTransferHistoryResponse getTransferHistory(Integer clientId);
}
