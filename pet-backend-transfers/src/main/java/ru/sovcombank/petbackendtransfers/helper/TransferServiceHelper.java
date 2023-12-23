package ru.sovcombank.petbackendtransfers.helper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendtransfers.builder.RequestBuilder;
import ru.sovcombank.petbackendtransfers.builder.TransferBuilder;
import ru.sovcombank.petbackendtransfers.converter.CurrencyConverter;
import ru.sovcombank.petbackendtransfers.db.DatabaseChanger;
import ru.sovcombank.petbackendtransfers.exception.AccountNotFoundException;
import ru.sovcombank.petbackendtransfers.model.api.request.UpdateBalanceRequest;
import ru.sovcombank.petbackendtransfers.model.api.response.GetAccountResponse;
import ru.sovcombank.petbackendtransfers.model.dto.AccountDTO;
import ru.sovcombank.petbackendtransfers.model.entity.Transfer;
import ru.sovcombank.petbackendtransfers.model.enums.CurEnum;
import ru.sovcombank.petbackendtransfers.model.enums.TransferResponseMessagesEnum;
import ru.sovcombank.petbackendtransfers.model.enums.TypePaymentsEnum;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class TransferServiceHelper {
    private final CurrencyConverter currencyConverter;
    private final RequestBuilder requestBuilder;
    private final DatabaseChanger databaseChanger;
    private final TransferBuilder transferBuilder;
    private final KafkaTemplate<String, Transfer> kafkaTemplate;

    @Value("${kafka.topic.transfers-history-transaction}")
    private String kafkaTopic;

    public TransferServiceHelper(CurrencyConverter currencyConverter, RequestBuilder requestBuilder, DatabaseChanger databaseChanger, TransferBuilder transferBuilder, KafkaTemplate<String, Transfer> kafkaTemplate) {
        this.currencyConverter = currencyConverter;
        this.requestBuilder = requestBuilder;
        this.databaseChanger = databaseChanger;
        this.transferBuilder = transferBuilder;
        this.kafkaTemplate = kafkaTemplate;
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

    public void updateBalance(String cur, GetAccountResponse getAccountResponse, String accountNumberFrom, String accountNumberTo, BigDecimal transferAmount) {
        UpdateBalanceRequest updateBalanceRequestForAccountFrom = requestBuilder.createUpdateBalanceRequest(
                TypePaymentsEnum.DEBITING.getTypePayment(),
                transferAmount
        );

        BigDecimal amountByCur = getAmountByCur(cur, transferAmount, getAccountResponse
        );

        UpdateBalanceRequest updateBalanceRequestForAccountTo = requestBuilder.createUpdateBalanceRequest(
                TypePaymentsEnum.REPLENISHMENT.getTypePayment(),
                amountByCur
        );

        databaseChanger.updateAccountBalance(accountNumberFrom, updateBalanceRequestForAccountFrom);
        databaseChanger.updateAccountBalance(accountNumberTo, updateBalanceRequestForAccountTo);
    }

    public void saveAndSendTransfer(String accountNumberFrom, String accountNumberTo, BigDecimal amount, String cur) {
        Transfer transfer = transferBuilder.createTransferObject(accountNumberFrom, accountNumberTo, amount, cur);

        databaseChanger.saveTransfer(transfer);

        kafkaTemplate.send(kafkaTopic, transfer);
    }
}
