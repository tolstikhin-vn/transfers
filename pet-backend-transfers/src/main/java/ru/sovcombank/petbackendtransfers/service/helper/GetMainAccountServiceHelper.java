package ru.sovcombank.petbackendtransfers.service.helper;

import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendtransfers.exception.AccountNotFoundException;
import ru.sovcombank.petbackendtransfers.model.dto.AccountDTO;
import ru.sovcombank.petbackendtransfers.model.enums.TransferResponseMessagesEnum;

import java.util.List;

@Component
public class GetMainAccountServiceHelper {

    // Получение номера основного счета для совершения перевода по нему
    public String getMainAccount(List<AccountDTO> accountList) {
        for (AccountDTO accountDTO : accountList) {
            if (accountDTO.isMain()) {
                return accountDTO.getAccountNumber();
            }
        }
        throw new AccountNotFoundException(TransferResponseMessagesEnum.ACCOUNT_NOT_FOUND.getMessage());
    }
}
