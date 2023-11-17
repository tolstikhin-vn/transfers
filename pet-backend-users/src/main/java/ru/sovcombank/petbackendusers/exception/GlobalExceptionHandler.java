package ru.sovcombank.petbackendusers.exception;

import jakarta.validation.ConstraintViolation;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

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

    @ExceptionHandler(InternalServerErrorException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> handleInternalServerErrorException(InternalServerErrorException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleBadRequestException(BadRequestException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> invalidFields = ex.getBindingResult().getAllErrors()
                .stream()
                .map(error -> ((FieldError) error).getField())
                .collect(Collectors.toList());

        String errorMessage = "Некорректный запрос по полю: " + String.join(", ", invalidFields);

        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<String> handleConflictException(ConflictException e) {
        ConstraintViolationException cause = (ConstraintViolationException) e.getCause().getCause();
        String message = cause.getMessage();
        String regex = "Key \\((.*?)\\)=\\((.*?)\\)";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);

        String errorMessage = "";
        while (matcher.find()) {
            String field = matcher.group(1); // Поле
            String value = matcher.group(2); // Значение
            errorMessage = value + " с таким " + field + " уже зарегистрирован";
        }

        return new ResponseEntity<>(errorMessage, HttpStatus.CONFLICT);
    }
}
