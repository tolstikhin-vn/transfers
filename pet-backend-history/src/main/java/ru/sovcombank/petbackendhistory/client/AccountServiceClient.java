package ru.sovcombank.petbackendhistory.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.sovcombank.petbackendhistory.exception.UserNotFoundException;
import ru.sovcombank.petbackendhistory.mapping.impl.ResponseToGetAccountsResponse;
import ru.sovcombank.petbackendhistory.model.api.response.GetAccountsResponse;
import ru.sovcombank.petbackendhistory.model.enums.HistoryResponseMessagesEnum;

/**
 * Клиент для отправки запросов в микросервис accounts.
 */
@Component
public class AccountServiceClient {

    private final RestTemplate restTemplate;

    private final ResponseToGetAccountsResponse responseToGetAccountsResponse;

    private final String accountServiceUrl;

    public AccountServiceClient(
            RestTemplate restTemplate,
            ResponseToGetAccountsResponse responseToGetAccountsResponse,
            @Value("${account-service.url}") String accountServiceUrl) {
        this.restTemplate = restTemplate;
        this.responseToGetAccountsResponse = responseToGetAccountsResponse;
        this.accountServiceUrl = accountServiceUrl;
    }

    /**
     * Получает список счетов для указанного клиента.
     *
     * @param clientId Идентификатор клиента.
     * @return Объект GetAccountsResponse с информацией о счетах клиента.
     * @throws UserNotFoundException Если клиент не найден, выбрасывается исключение.
     */
    public GetAccountsResponse getAccountsResponse(String clientId) {
        String getAccountsUrl = accountServiceUrl + "/accounts/" + clientId;

        ResponseEntity<Object> responseEntity = restTemplate.getForEntity(getAccountsUrl, Object.class);
        if (!responseEntity.getStatusCode().isError()) {
            return responseToGetAccountsResponse.map(responseEntity);
        }
        throw new UserNotFoundException(HistoryResponseMessagesEnum.USER_NOT_FOUND.getMessage());
    }
}
