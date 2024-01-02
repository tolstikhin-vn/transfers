package ru.sovcombank.petbackendtransfers.service.helper;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sovcombank.petbackendtransfers.builder.RequestBuilder;
import ru.sovcombank.petbackendtransfers.db.DatabaseChanger;
import ru.sovcombank.petbackendtransfers.model.api.request.UpdateBalanceRequest;
import ru.sovcombank.petbackendtransfers.model.api.response.GetAccountResponse;
import ru.sovcombank.petbackendtransfers.model.entity.Transfer;
import ru.sovcombank.petbackendtransfers.model.enums.TypePaymentsEnum;

import java.math.BigDecimal;

@Component
public class UpdateBalanceServiceHelper {

    private final RequestBuilder requestBuilder;

    private final DatabaseChanger databaseChanger;

    private final GetAmountByCurServiceHelper getAmountByCurServiceHelper;

    private final SaveTransferServiceHelper saveTransferServiceHelper;

    public UpdateBalanceServiceHelper(RequestBuilder requestBuilder,
                                      DatabaseChanger databaseChanger,
                                      GetAmountByCurServiceHelper getAmountByCurServiceHelper,
                                      SaveTransferServiceHelper saveTransferServiceHelper) {
        this.requestBuilder = requestBuilder;
        this.databaseChanger = databaseChanger;
        this.getAmountByCurServiceHelper = getAmountByCurServiceHelper;
        this.saveTransferServiceHelper = saveTransferServiceHelper;
    }

    @Transactional
    public Transfer updateBalance(String cur, GetAccountResponse getAccountResponse, String accountNumberFrom, String accountNumberTo, BigDecimal transferAmount) {
        UpdateBalanceRequest updateBalanceRequestForAccountFrom = requestBuilder.createUpdateBalanceRequest(
                TypePaymentsEnum.DEBITING.getTypePayment(),
                transferAmount);

        BigDecimal amountByCur = getAmountByCurServiceHelper.getAmountByCur(cur, transferAmount, getAccountResponse);

        UpdateBalanceRequest updateBalanceRequestForAccountTo = requestBuilder.createUpdateBalanceRequest(
                TypePaymentsEnum.REPLENISHMENT.getTypePayment(),
                amountByCur);

        databaseChanger.updateAccountBalance(accountNumberFrom, updateBalanceRequestForAccountFrom);
        databaseChanger.updateAccountBalance(accountNumberTo, updateBalanceRequestForAccountTo);

        return saveTransferServiceHelper.saveTransfer(accountNumberFrom, accountNumberTo, transferAmount, cur);
    }
}
