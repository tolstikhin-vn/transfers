package ru.sovcombank.petbackendtransfers.builder;

import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendtransfers.model.entity.Transfer;

import java.math.BigDecimal;

@Component
public class TransferBuilder {

    // Создание сущности Transfer по значениям полей
    public Transfer createTransferObject(
            String accountNumberFrom,
            String accountNumberTo,
            BigDecimal transferAmount,
            String currency) {
        Transfer transfer = new Transfer();
        transfer.setAccountNumberFrom(accountNumberFrom);
        transfer.setAccountNumberTo(accountNumberTo);
        transfer.setAmount(transferAmount);
        transfer.setCur(currency);

        return transfer;
    }
}
