package ru.sovcombank.petbackendhistory.model.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sovcombank.petbackendhistory.model.dto.HistoryDTO;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetTransferHistoryResponse {

    private Integer clientId;

    private List<HistoryDTO> transfers;
}