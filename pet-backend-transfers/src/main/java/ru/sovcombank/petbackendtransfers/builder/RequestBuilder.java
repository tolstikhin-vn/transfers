package ru.sovcombank.petbackendtransfers.builder;

import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendtransfers.model.api.request.UpdateBalanceRequest;

import java.math.BigDecimal;

@Component
public class RequestBuilder {

    // Создание запроса UpdateBalanceRequest по данным
    public UpdateBalanceRequest createUpdateBalanceRequest(String typePayment, BigDecimal amount) {
        UpdateBalanceRequest updateBalanceRequest = new UpdateBalanceRequest();
        updateBalanceRequest.setTypePayments(typePayment);
        updateBalanceRequest.setAmount(amount);
        return updateBalanceRequest;
    }
}
