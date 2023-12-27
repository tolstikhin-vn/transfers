package ru.sovcombank.petbackendhistory.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sovcombank.petbackendhistory.model.api.response.GetTransferHistoryResponse;
import ru.sovcombank.petbackendhistory.service.HistoryService;


/**
 * Контроллер для управления историями операций.
 */
@RestController
@RequestMapping("/history")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    /**
     * Обрабатывает запрос на получение истории операций.
     *
     * @param clientId Идентификатор клиента.
     * @return Ответ с информацией о переводе.
     */
    @GetMapping("/{clientId}")
    public ResponseEntity<Object> getTransferHistory(@PathVariable String clientId) {
        GetTransferHistoryResponse response = historyService.getTransferHistory(clientId);
        return ResponseEntity.ok(response);
    }
}
