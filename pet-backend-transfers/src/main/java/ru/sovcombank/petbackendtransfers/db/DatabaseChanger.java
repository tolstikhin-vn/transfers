package ru.sovcombank.petbackendtransfers.db;

import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendtransfers.client.AccountServiceClient;
import ru.sovcombank.petbackendtransfers.model.api.request.UpdateBalanceRequest;
import ru.sovcombank.petbackendtransfers.model.entity.Transfer;
import ru.sovcombank.petbackendtransfers.repository.TransferRepository;

@Component
public class DatabaseChanger {

    private final TransferRepository transferRepository;

    private final AccountServiceClient accountServiceClient;

    public DatabaseChanger(TransferRepository transferRepository, AccountServiceClient accountServiceClient) {
        this.transferRepository = transferRepository;
        this.accountServiceClient = accountServiceClient;
    }

    // Обновление баланса
    public void updateAccountBalance(String accountNumber, UpdateBalanceRequest updateBalanceRequest) {
        accountServiceClient.updateBalance(accountNumber, updateBalanceRequest);
    }

    // Сохранение перевода в базе данных
    public void saveTransfer(Transfer transfer) {
        transferRepository.save(transfer);
    }
}
