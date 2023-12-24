package ru.sovcombank.petbackendhistory.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sovcombank.petbackendhistory.model.api.response.MessageResponse;

/**
 * Глобальный обработчик исключений для управления ошибками в приложении.
 */
@ControllerAdvice
@Component
public class GlobalExceptionHandler {

    private static final String INTERNAL_SERVER_ERROR = "Internal Server Error";

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<MessageResponse> handleException() {
        return new ResponseEntity<>(new MessageResponse(INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<MessageResponse> handleUserNotFoundException(UserNotFoundException ex) {
        return new ResponseEntity<>(new MessageResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
    }
}