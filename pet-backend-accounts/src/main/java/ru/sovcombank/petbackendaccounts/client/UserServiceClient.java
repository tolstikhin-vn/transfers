package ru.sovcombank.petbackendaccounts.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.sovcombank.petbackendaccounts.exception.UserNotFoundException;
import ru.sovcombank.petbackendaccounts.mapping.impl.ResponseToGetUserResponse;
import ru.sovcombank.petbackendaccounts.model.api.response.GetUserResponse;
import ru.sovcombank.petbackendaccounts.model.enums.AccountResponseMessagesEnum;

@Component
public class UserServiceClient {

    private final RestTemplate restTemplate;
    private final ResponseToGetUserResponse responseToGetUserResponse;
    private final String userServiceUrl;

    public UserServiceClient(RestTemplate restTemplate,
                             ResponseToGetUserResponse responseToGetUserResponse,
                             @Value("${user-service.url}") String userServiceUrl) {
        this.restTemplate = restTemplate;
        this.responseToGetUserResponse = responseToGetUserResponse;
        this.userServiceUrl = userServiceUrl;
    }

    // Проверка существования клиента с таким clientId
    public ResponseEntity<Object> checkUserExists(String clientId) {
        String getUserByIdUrl = userServiceUrl + "/users/" + clientId;

        try {
            ResponseEntity<Object> responseEntity = restTemplate.getForEntity(getUserByIdUrl, Object.class);
            if (!responseEntity.getStatusCode().isError()) {
                GetUserResponse getUserResponseEntity = responseToGetUserResponse.map(responseEntity);
                if (!isUserActiveAndNotDeleted(getUserResponseEntity)) {
                    throw new UserNotFoundException(AccountResponseMessagesEnum.USER_NOT_FOUND.getMessage());
                }
            }
            return responseEntity;

        } catch (HttpClientErrorException.NotFound ex) {
            throw new UserNotFoundException(AccountResponseMessagesEnum.USER_NOT_FOUND.getMessage());
        }
    }

    /**
     * Проверяет, активен ли пользователь и не удален ли он.
     *
     * @param getUserResponseEntity Объект GetUserResponse для проверки.
     * @return true, если пользователь активен и не удален, в противном случае - false.
     */
    private boolean isUserActiveAndNotDeleted(GetUserResponse getUserResponseEntity) {
        return getUserResponseEntity.isActive() && !getUserResponseEntity.isDeleted();
    }
}
