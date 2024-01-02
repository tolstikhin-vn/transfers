package ru.sovcombank.petbackendusers.exception;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sovcombank.petbackendusers.model.api.response.MessageResponse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Глобальный обработчик исключений для управления ошибками в приложении.
 */
@ControllerAdvice
@Component
public class GlobalExceptionHandler {

    private static final String INTERNAL_SERVER_ERROR = "Internal Server Error";

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<MessageResponse> handleException(Exception ex) {
        return new ResponseEntity<>(new MessageResponse(INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InternalServerErrorException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<MessageResponse> handleInternalServerErrorException(BadRequestException ex) {
        return new ResponseEntity<>(new MessageResponse(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<MessageResponse> handleBadRequestException(BadRequestException ex) {
        return new ResponseEntity<>(new MessageResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<MessageResponse> handleUserNotFoundException(UserNotFoundException ex) {
        return new ResponseEntity<>(new MessageResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<MessageResponse> handleValidationException(MethodArgumentNotValidException ex) {
        FieldError invalidField = ex.getBindingResult().getFieldError();
        String errorMessage = String.format("Некорректный запрос по полю %s", invalidField.getField());
        return new ResponseEntity<>(new MessageResponse(errorMessage), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<MessageResponse> handleConflictException(ConflictException ex) {
        ConstraintViolationException cause = (ConstraintViolationException) ex.getCause().getCause();
        String message = cause.getMessage();
        String regex = "Key \\((.*?)\\)=\\((.*?)\\)"; // Регулярка, чтобы достать поля и значения из ответа с ошибкой

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);

        String errorMessage = "";
        while (matcher.find()) {
            String value = matcher.group(2); // Значение
            errorMessage = String.format("%s с таким %s уже зарегистрирован", value, getErrorField(matcher.group(1)));
        }

        return new ResponseEntity<>(new MessageResponse(errorMessage), HttpStatus.CONFLICT);
    }

    private String getErrorField(String field) {
        return switch (field) {
            case "phone_number" -> "номером телефона";
            case "passport_number" -> "номером паспорта";
            default -> "";
        };
    }
}
