package ru.sovcombank.petbackendusers.builder;

import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendusers.model.api.response.CreateUserResponse;
import ru.sovcombank.petbackendusers.model.enums.UserMessagesEnum;

@Component
public class ResponseBuilder {

    public CreateUserResponse buildCreateUserResponse(Integer userId) {
        CreateUserResponse createUserResponse = new CreateUserResponse();
        createUserResponse.setClientId(userId);
        createUserResponse.setMessage(UserMessagesEnum.USER_CREATED_SUCCESSFULLY_MESSAGE.getMessage());
        return createUserResponse;
    }
}
