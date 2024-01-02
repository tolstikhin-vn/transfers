package ru.sovcombank.petbackendhistory.service.impl;

import org.springframework.stereotype.Service;
import ru.sovcombank.petbackendhistory.mapping.impl.ListTransferToGetHistoryTransferResponse;
import ru.sovcombank.petbackendhistory.model.api.response.GetTransferHistoryResponse;
import ru.sovcombank.petbackendhistory.model.entity.History;
import ru.sovcombank.petbackendhistory.repository.HistoryRepository;
import ru.sovcombank.petbackendhistory.service.HistoryService;

import java.util.List;

/**
 * Сервис для операций с историей транзакций.
 */
@Service
public class HistoryServiceImpl implements HistoryService {

    private final HistoryRepository historyRepository;

    private final ListTransferToGetHistoryTransferResponse listTransferToGetHistoryTransferResponse;

    public HistoryServiceImpl(
            HistoryRepository historyRepository,
            ListTransferToGetHistoryTransferResponse listTransferToGetHistoryTransferResponse) {
        this.historyRepository = historyRepository;
        this.listTransferToGetHistoryTransferResponse = listTransferToGetHistoryTransferResponse;
    }

    @Override
    public GetTransferHistoryResponse getTransferHistory(Integer clientId) {
        List<History> transferHistory = historyRepository.findByClientId(clientId);

        listTransferToGetHistoryTransferResponse.setClientId(clientId);

        return listTransferToGetHistoryTransferResponse.map(transferHistory);
    }
}
