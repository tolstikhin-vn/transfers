package ru.sovcombank.petbackendhistory.mapping.impl;

import lombok.Data;
import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendhistory.mapping.Mapper;
import ru.sovcombank.petbackendhistory.model.api.response.GetTransferHistoryResponse;
import ru.sovcombank.petbackendhistory.model.dto.HistoryDTO;
import ru.sovcombank.petbackendhistory.model.entity.History;

import java.util.List;

@Data
@Component
public class ListTransferToGetHistoryTransferResponse implements Mapper<List<History>, GetTransferHistoryResponse> {

    private final Mapper<History, HistoryDTO> historyToHistoryDTO;

    private String clientId;

    public ListTransferToGetHistoryTransferResponse(Mapper<History, HistoryDTO> historyToHistoryDTO) {
        this.historyToHistoryDTO = historyToHistoryDTO;
    }

    @Override
    public GetTransferHistoryResponse map(List<History> histories) {
        List<HistoryDTO> historyDTOs = histories.stream()
                .map(historyToHistoryDTO::map)
                .toList();

        GetTransferHistoryResponse response = new GetTransferHistoryResponse();
        response.setClientId(clientId);
        response.setTransfers(historyDTOs);

        return response;
    }
}
