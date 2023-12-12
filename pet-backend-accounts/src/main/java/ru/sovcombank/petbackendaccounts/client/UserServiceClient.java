package ru.sovcombank.petbackendaccounts.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.sovcombank.petbackendaccounts.exception.UserNotFoundException;
import ru.sovcombank.petbackendaccounts.model.enums.AccountResponseMessagesEnum;

/**
 * Клиент для отправки запросов в микросервис users.
 */
@Component
public class UserServiceClient {

    private final RestTemplate restTemplate;
    private final String userServiceUrl;

    public UserServiceClient(RestTemplate restTemplate,
                             @Value("${user-service.url}") String userServiceUrl) {
        this.restTemplate = restTemplate;
        this.userServiceUrl = userServiceUrl;
    }

    // Проверка существования клиента с таким clientId
    public ResponseEntity<Object> checkUserExists(String clientId) {
        String getUserByIdUrl = userServiceUrl + "/users/" + clientId;

        ResponseEntity<Object> responseEntity = restTemplate.getForEntity(getUserByIdUrl, Object.class);
        if (responseEntity.getStatusCode().isError()) {
            throw new UserNotFoundException(AccountResponseMessagesEnum.USER_NOT_FOUND.getMessage());
        }
        return responseEntity;
    }
}
