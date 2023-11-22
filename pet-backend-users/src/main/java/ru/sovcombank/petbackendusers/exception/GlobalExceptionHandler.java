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
import ru.sovcombank.petbackendusers.api.response.MessageErrorResponse;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Глобальный обработчик исключений для управления ошибками в приложении.
 */
@ControllerAdvice
@Component
public class GlobalExceptionHandler {

    private static final String INTERNAL_SERVER_ERROR = "Internal Server Error";

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<MessageErrorResponse> handleException(Exception ex) {
        return new ResponseEntity<>(new MessageErrorResponse(INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InternalServerErrorException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<MessageErrorResponse> handleInternalServerErrorException(BadRequestException ex) {
        return new ResponseEntity<>(new MessageErrorResponse(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<MessageErrorResponse> handleBadRequestException(BadRequestException ex) {
        return new ResponseEntity<>(new MessageErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<MessageErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
        return new ResponseEntity<>(new MessageErrorResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<MessageErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> invalidFields = ex.getBindingResult().getAllErrors()
                .stream()
                .map(error -> ((FieldError) error).getField())
                .collect(Collectors.toList());

        String errorMessage = "Некорректный запрос по полю: " + String.join(", ", invalidFields);

        return new ResponseEntity<>(new MessageErrorResponse(errorMessage), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<MessageErrorResponse> handleConflictException(ConflictException ex) {
        ConstraintViolationException cause = (ConstraintViolationException) ex.getCause().getCause();
        String message = cause.getMessage();
        String regex = "Key \\((.*?)\\)=\\((.*?)\\)"; // Регулярка, чтобы достать поля и значения из ответа с ошибкой

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);

        String errorMessage = "";
        while (matcher.find()) {
            String field = matcher.group(1); // Поле
            String value = matcher.group(2); // Значение
            errorMessage = value + " с таким " + field + " уже зарегистрирован";
        }

        return new ResponseEntity<>(new MessageErrorResponse(errorMessage), HttpStatus.CONFLICT);
    }
}
