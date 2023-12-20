package ru.sovcombank.petbackendtransfers.helper;

import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendtransfers.converter.CurrencyConverter;
import ru.sovcombank.petbackendtransfers.exception.AccountNotFoundException;
import ru.sovcombank.petbackendtransfers.model.api.response.GetAccountResponse;
import ru.sovcombank.petbackendtransfers.model.dto.AccountDTO;
import ru.sovcombank.petbackendtransfers.model.enums.CurEnum;
import ru.sovcombank.petbackendtransfers.model.enums.TransferResponseMessagesEnum;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class TransferHelper {

    private final CurrencyConverter currencyConverter;

    public TransferHelper(CurrencyConverter currencyConverter) {
        this.currencyConverter = currencyConverter;
    }

    // Получение суммы перевода после конвертации валют
    public BigDecimal getAmountByCur(String curFrom, BigDecimal amount, GetAccountResponse getAccountResponse) {

        String curTo = getAccountResponse.getCur();

        // Если валюты в счетах отправителя и получателя совпадают
        if (curFrom.equals(CurEnum.RUB.getCur()) && curTo.equals(CurEnum.RUB.getCur())) {
            return amount;
            // Если валюты в счетах отправителя и получателя не RUB (810)
        } else if (!curFrom.equals(CurEnum.RUB.getCur()) && !curTo.equals(CurEnum.RUB.getCur())) {
            return amount.multiply(BigDecimal.valueOf(
                    currencyConverter.getCurrentRate(curFrom) / currencyConverter.getCurrentRate(curTo))
            );
            // Если валюта счета отправителя RUB (810), а получателя - нет
        } else if (curFrom.equals(CurEnum.RUB.getCur())) {
            return amount.divide(BigDecimal.valueOf(currencyConverter.getCurrentRate(curTo)), 2, RoundingMode.HALF_UP);
            // Если валюта счета отправителя не RUB (810), а получателя - RUB
        } else {
            return amount.multiply(BigDecimal.valueOf(currencyConverter.getCurrentRate(curFrom)));
        }
    }

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
