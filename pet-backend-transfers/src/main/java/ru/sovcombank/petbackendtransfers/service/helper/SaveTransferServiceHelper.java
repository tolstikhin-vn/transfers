package ru.sovcombank.petbackendtransfers.service.helper;

import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendtransfers.builder.TransferBuilder;
import ru.sovcombank.petbackendtransfers.db.DatabaseChanger;
import ru.sovcombank.petbackendtransfers.model.entity.Transfer;

import java.math.BigDecimal;

@Component
public class SaveTransferServiceHelper {
    private final DatabaseChanger databaseChanger;
    private final TransferBuilder transferBuilder;

    public SaveTransferServiceHelper(DatabaseChanger databaseChanger, TransferBuilder transferBuilder) {
        this.databaseChanger = databaseChanger;
        this.transferBuilder = transferBuilder;
    }

    public Transfer saveTransfer(String accountNumberFrom, String accountNumberTo, BigDecimal amount, String cur) {
        Transfer transfer = transferBuilder.createTransferObject(accountNumberFrom, accountNumberTo, amount, cur);
        databaseChanger.saveTransfer(transfer);
        return transfer;
    }
}
