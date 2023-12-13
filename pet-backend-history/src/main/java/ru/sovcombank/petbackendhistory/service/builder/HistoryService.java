package ru.sovcombank.petbackendhistory.service.builder;

import ru.sovcombank.petbackendhistory.model.api.response.GetTransferHistoryResponse;

public interface HistoryService {

    GetTransferHistoryResponse getTransferHistory(String clientId);
}
