package ru.sovcombank.petbackendusers.service.validator;

import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendusers.exception.UserNotFoundException;
import ru.sovcombank.petbackendusers.model.entity.User;
import ru.sovcombank.petbackendusers.model.enums.UserMessagesEnum;

@Component
public class UserValidator {

    // Валидация пользователя (проверка поля isDeleted)
    public void validateUserIsDeleted(User user) {
        if (user.getIsDeleted()) {
            throw new UserNotFoundException(UserMessagesEnum.USER_NOT_FOUND_MESSAGE.getMessage());
        }
    }
}
