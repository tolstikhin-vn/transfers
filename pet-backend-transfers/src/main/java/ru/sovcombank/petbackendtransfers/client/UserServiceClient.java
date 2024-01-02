package ru.sovcombank.petbackendtransfers.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.sovcombank.petbackendtransfers.exception.InternalServerErrorException;
import ru.sovcombank.petbackendtransfers.exception.UserNotFoundException;
import ru.sovcombank.petbackendtransfers.mapping.impl.ResponseToGetUserResponse;
import ru.sovcombank.petbackendtransfers.model.api.response.GetUserResponse;
import ru.sovcombank.petbackendtransfers.model.enums.TransferResponseMessagesEnum;

/**
 * Клиент для отправки запросов в микросервис users.
 */
@Component
public class UserServiceClient {

    private final RestTemplate restTemplate;
    private final ResponseToGetUserResponse responseToGetUserResponse;
    private final String userServiceUrl;

    public UserServiceClient(
            RestTemplate restTemplate,
            ResponseToGetUserResponse responseToGetUserResponse,
            @Value("${user-service.url}") String userServiceUrl) {
        this.restTemplate = restTemplate;
        this.responseToGetUserResponse = responseToGetUserResponse;
        this.userServiceUrl = userServiceUrl;
    }

    private ResponseEntity<Object> getResponseEntity(String url) {
        return restTemplate.getForEntity(url, Object.class);
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

    /**
     * Проверяет существование пользователя для осуществления трансфера по номеру счета.
     *
     * @param clientId Идентификатор клиента.
     * @return true, если пользователь существует и активен, в противном случае - false.
     */
    public boolean checkUserExistsForTransferByAccount(Integer clientId) {
        String getUserByIdUrl = userServiceUrl + "/users/" + clientId;

        try {
            ResponseEntity<Object> responseEntity = getResponseEntity(getUserByIdUrl);
            if (!responseEntity.getStatusCode().isError()) {
                GetUserResponse getUserResponseEntity = responseToGetUserResponse.map(responseEntity);
                return isUserActiveAndNotDeleted(getUserResponseEntity);
            }
        } catch (HttpClientErrorException.NotFound ex) {
            return false;
        }
        return false;
    }

    /**
     * Проверяет существование пользователя для осуществления трансфера по номеру телефона.
     *
     * @param clientId    Идентификатор клиента (номер счета).
     * @param phoneNumber Номер телефона пользователя.
     * @return true, если пользователь существует, активен и номер телефона совпадает,
     * в противном случае - false.
     */
    public boolean checkUserExistsForTransferByPhone(Integer clientId, String phoneNumber) {
        String getUserByIdUrl = userServiceUrl + "/users/" + clientId;

        try {
            ResponseEntity<Object> responseEntity = getResponseEntity(getUserByIdUrl);
            if (!responseEntity.getStatusCode().isError()) {
                GetUserResponse getUserResponseEntity = responseToGetUserResponse.map(responseEntity);
                return isUserActiveAndNotDeleted(getUserResponseEntity)
                        && getUserResponseEntity.getPhoneNumber().equals(phoneNumber);
            }
        } catch (HttpClientErrorException.NotFound ex) {
            return false;
        }
        return false;
    }

    /**
     * Получает информацию о пользователе по номеру телефона.
     *
     * @param phoneNumber Номер телефона пользователя.
     * @return Объект GetUserResponse с информацией о пользователе.
     * @throws UserNotFoundException Если пользователь не найден, выбрасывается исключение.
     */
    public GetUserResponse getUserInfo(String phoneNumber) {
        String getUserByPhoneNumberUrl = userServiceUrl + "/users/phone-number/" + phoneNumber;

        try {
            ResponseEntity<Object> responseEntity = getResponseEntity(getUserByPhoneNumberUrl);
            if (!responseEntity.getStatusCode().isError()) {
                return responseToGetUserResponse.map(responseEntity);
            }
        } catch (HttpClientErrorException.NotFound ex) {
            throw new UserNotFoundException(TransferResponseMessagesEnum.USER_NOT_FOUND.getMessage());
        }
        throw new InternalServerErrorException();
    }
}