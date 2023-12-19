package ru.sovcombank.petbackendtransfers.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sovcombank.petbackendtransfers.model.api.response.MakeTransferResponse;
import ru.sovcombank.petbackendtransfers.model.api.response.MessageResponse;

/**
 * Глобальный обработчик исключений для управления ошибками в приложении.
 */
@ControllerAdvice
@Component
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<MessageResponse> handleValidationException(MethodArgumentNotValidException ex) {
        FieldError invalidField = ex.getBindingResult().getFieldError();
        String errorMessage = String.format("Некорректный запрос по полю %s", invalidField.getField());
        return new ResponseEntity<>(new MessageResponse(errorMessage), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InternalServerErrorException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<MessageResponse> handleInternalServerErrorException(InternalServerErrorException ex) {
        return new ResponseEntity<>(new MessageResponse(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<MakeTransferResponse> handleUserNotFoundException(UserNotFoundException ex) {
        return new ResponseEntity<>(new MakeTransferResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AccountClosedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<MessageResponse> handleAccountClosedException(AccountClosedException ex) {
        return new ResponseEntity<>(new MessageResponse(ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<MessageResponse> handleAccountNotFoundException(AccountNotFoundException ex) {
        return new ResponseEntity<>(new MessageResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ResponseEntity<MessageResponse> handleAccountNotFoundException(InsufficientFundsException ex) {
        return new ResponseEntity<>(new MessageResponse(ex.getMessage()), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<MessageResponse> handleBadRequestException(BadRequestException ex) {
        return new ResponseEntity<>(new MessageResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TransferNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<MessageResponse> handleTransferNotFoundException(TransferNotFoundException ex) {
        return new ResponseEntity<>(new MessageResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
    }
}