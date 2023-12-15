package ru.sovcombank.petbackendhistory.service.impl;

import org.springframework.stereotype.Service;
import ru.sovcombank.petbackendhistory.client.AccountServiceClient;
import ru.sovcombank.petbackendhistory.mapping.impl.ListTransferToGetHistoryTransferResponse;
import ru.sovcombank.petbackendhistory.model.api.response.GetAccountsResponse;
import ru.sovcombank.petbackendhistory.model.api.response.GetTransferHistoryResponse;
import ru.sovcombank.petbackendhistory.model.dto.AccountDTO;
import ru.sovcombank.petbackendhistory.model.entity.History;
import ru.sovcombank.petbackendhistory.repository.HistoryRepository;
import ru.sovcombank.petbackendhistory.service.builder.HistoryService;

import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для операций с историей транзакций.
 */
@Service
public class HistoryServiceImpl implements HistoryService {

    private final HistoryRepository historyRepository;

    private final AccountServiceClient accountServiceClient;

    private final ListTransferToGetHistoryTransferResponse listTransferToGetHistoryTransferResponse;

    public HistoryServiceImpl(
            HistoryRepository historyRepository,
            AccountServiceClient accountServiceClient,
            ListTransferToGetHistoryTransferResponse listTransferToGetHistoryTransferResponse) {
        this.historyRepository = historyRepository;
        this.accountServiceClient = accountServiceClient;
        this.listTransferToGetHistoryTransferResponse = listTransferToGetHistoryTransferResponse;
    }

    @Override
    public GetTransferHistoryResponse getTransferHistory(String clientId) {
        List<History> transferHistory = historyRepository.findByAccountNumbers(getAccountNumbersList(clientId));

        listTransferToGetHistoryTransferResponse.setClientId(clientId);

        return listTransferToGetHistoryTransferResponse.map(transferHistory);
    }

    /**
     * Получает историю транзакций для указанного клиента.
     *
     * @param clientId Идентификатор клиента.
     * @return Ответ с историей транзакций.
     */
    private List<String> getAccountNumbersList(String clientId) {
        GetAccountsResponse getAccountsResponse = accountServiceClient.getAccountsResponse(clientId);
        List<String> accountNumbersList = new ArrayList<>();

        for (AccountDTO accountDTO : getAccountsResponse.getAccountList()) {
            accountNumbersList.add(accountDTO.getAccountNumber());
        }

        return accountNumbersList;
    }
}
