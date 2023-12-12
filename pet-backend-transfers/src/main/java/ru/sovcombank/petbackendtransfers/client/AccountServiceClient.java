package ru.sovcombank.petbackendtransfers.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.sovcombank.petbackendtransfers.exception.AccountNotFoundException;
import ru.sovcombank.petbackendtransfers.exception.InternalServerErrorException;
import ru.sovcombank.petbackendtransfers.mapping.impl.ResponseToGetAccountResponse;
import ru.sovcombank.petbackendtransfers.mapping.impl.ResponseToGetAccountsResponse;
import ru.sovcombank.petbackendtransfers.mapping.impl.ResponseToGetBalanceResponse;
import ru.sovcombank.petbackendtransfers.model.api.request.UpdateBalanceRequest;
import ru.sovcombank.petbackendtransfers.model.api.response.GetAccountResponse;
import ru.sovcombank.petbackendtransfers.model.api.response.GetAccountsResponse;
import ru.sovcombank.petbackendtransfers.model.api.response.GetBalanceResponse;
import ru.sovcombank.petbackendtransfers.model.enums.TransferResponseMessagesEnum;

/**
 * Клиент для отправки запросов в микросервис accounts.
 */
@Component
public class AccountServiceClient {

    private final RestTemplate restTemplate;

    private final ResponseToGetAccountResponse responseToGetAccountResponse;
    private final ResponseToGetAccountsResponse responseToGetAccountsResponse;
    private final ResponseToGetBalanceResponse responseToGetBalanceResponse;

    private final String accountServiceUrl;

    public AccountServiceClient(
            RestTemplate restTemplate,
            ResponseToGetAccountResponse responseToGetAccountResponse,
            ResponseToGetAccountsResponse responseToGetAccountsResponse,
            ResponseToGetBalanceResponse responseToGetBalanceResponse,
            @Value("${account-service.url}") String accountServiceUrl) {
        this.restTemplate = restTemplate;
        this.responseToGetAccountResponse = responseToGetAccountResponse;
        this.responseToGetAccountsResponse = responseToGetAccountsResponse;
        this.responseToGetBalanceResponse = responseToGetBalanceResponse;
        this.accountServiceUrl = accountServiceUrl;
    }

    /**
     * Получает информацию о счете по его номеру.
     *
     * @param accountNumber Номер счета.
     * @return Объект GetAccountResponse с информацией о счете.
     * @throws AccountNotFoundException Если счет не найден, выбрасывается исключение.
     */
    public GetAccountResponse getAccountResponse(String accountNumber) {
        String getAccountByAccountNumberUrl = accountServiceUrl + "/accounts/account/" + accountNumber;

        ResponseEntity<Object> responseEntity = restTemplate.getForEntity(getAccountByAccountNumberUrl, Object.class);
        if (!responseEntity.getStatusCode().isError()) {
            return responseToGetAccountResponse.map(responseEntity);
        }

        throw new AccountNotFoundException(TransferResponseMessagesEnum.ACCOUNT_NOT_FOUND.getMessage());
    }

    /**
     * Обновляет баланс счета.
     *
     * @param accountNumber        Номер счета.
     * @param updateBalanceRequest Запрос на обновление баланса.
     * @throws InternalServerErrorException Если баланс не удалось обновить, выбрасывается исключение.
     */
    public void updateBalance(String accountNumber, UpdateBalanceRequest updateBalanceRequest) {
        String updateBalanceUrl = accountServiceUrl + "/accounts/balance/" + accountNumber;

        try {
            restTemplate.exchange(
                    updateBalanceUrl,
                    HttpMethod.PUT,
                    new HttpEntity<>(updateBalanceRequest),
                    Object.class);
        } catch (Exception ex) {
            throw new InternalServerErrorException(ex);
        }
    }

    /**
     * Получает список счетов для указанного клиента.
     *
     * @param clientId Идентификатор клиента.
     * @return Объект GetAccountsResponse с информацией о счетах клиента.
     * @throws AccountNotFoundException Если клиент не найден, выбрасывается исключение.
     */
    public GetAccountsResponse getAccountsResponse(String clientId) {
        String getAccountsUrl = accountServiceUrl + "/accounts/" + clientId;

        ResponseEntity<Object> responseEntity = restTemplate.getForEntity(getAccountsUrl, Object.class);
        if (!responseEntity.getStatusCode().isError()) {
            return responseToGetAccountsResponse.map(responseEntity);
        }
        throw new AccountNotFoundException(TransferResponseMessagesEnum.USER_NOT_FOUND.getMessage());
    }

    /**
     * Получает информацию о балансе счета.
     *
     * @param accountNumber Номер счета.
     * @return Объект GetBalanceResponse с информацией о балансе.
     * @throws AccountNotFoundException Если счет не найден, выбрасывается исключение.
     */
    public GetBalanceResponse getBalanceResponse(String accountNumber) {
        String getAccountsUrl = accountServiceUrl + "/accounts/balance/" + accountNumber;

        ResponseEntity<Object> responseEntity = restTemplate.getForEntity(getAccountsUrl, Object.class);
        if (!responseEntity.getStatusCode().isError()) {
            return responseToGetBalanceResponse.map(responseEntity);
        }
        throw new AccountNotFoundException(TransferResponseMessagesEnum.ACCOUNT_NOT_FOUND.getMessage());
    }
}