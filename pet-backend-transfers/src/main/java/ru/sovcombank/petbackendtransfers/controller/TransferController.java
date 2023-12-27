package ru.sovcombank.petbackendtransfers.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sovcombank.petbackendtransfers.model.api.response.GetTransferResponse;
import ru.sovcombank.petbackendtransfers.model.api.response.MakeTransferResponse;
import ru.sovcombank.petbackendtransfers.service.TransferService;

import java.util.Map;

/**
 * Контроллер для управления переводами.
 */
@RestController
@RequestMapping("/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    /**
     * Обрабатывает запрос на создание нового перевода.
     *
     * @param requestMap Запрос на создание перевода.
     * @return Ответ с результатом выполнения перевода.
     */
    @PostMapping()
    public ResponseEntity<MakeTransferResponse> makeTransfer(@RequestBody Map<String, Object> requestMap) {
        MakeTransferResponse makeTransferResponse = transferService.makeTransfer(requestMap);
        return ResponseEntity.ok(makeTransferResponse);
    }

    /**
     * Обрабатывает запрос на получение информации о переводе.
     *
     * @param uuid UUID транзакции.
     * @return Ответ с информацией о переводе.
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<Object> getInfoAboutTransaction(@PathVariable String uuid) {
        GetTransferResponse response = transferService.getTransfers(uuid);
        return ResponseEntity.ok(response);
    }
}