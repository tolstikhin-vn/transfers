package ru.sovcombank.petbackendaccounts.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.sovcombank.petbackendaccounts.exception.UserNotFoundException;
import ru.sovcombank.petbackendaccounts.model.enums.AccountResponseMessagesEnum;

@Component
public class UserServiceClient {

    private final RestTemplate restTemplate;
    private final String userServiceUrl;

    public UserServiceClient(RestTemplate restTemplate, @Value("${user-service.url}") String userServiceUrl) {
        this.restTemplate = restTemplate;
        this.userServiceUrl = userServiceUrl;
    }

    // Проверка существования клиента с таким clientId
    public void checkUserExists(Integer clientId) {
        String getUserByIdUrl = userServiceUrl + "/users/" + clientId;

        System.out.println(getUserByIdUrl);

        try {
            restTemplate.getForObject(getUserByIdUrl, Object.class);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            throw new UserNotFoundException(AccountResponseMessagesEnum.USER_NOT_FOUND.getMessage());
        }
    }
}
