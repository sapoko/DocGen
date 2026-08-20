package Sapoko.docgen.common;

import Sapoko.docgen.document.InvalidInputValueException;
import Sapoko.docgen.generation.GenerationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(NotFoundException e) {
        log.warn(e.getMessage());
        return new ErrorResponse(e.getMessage(), Instant.now(), null);
    }

    @ExceptionHandler(InvalidInputValueException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidInput(InvalidInputValueException e) {
        log.warn(e.getMessage());
        return new ErrorResponse(e.getMessage(), Instant.now(), null);
    }

    @ExceptionHandler(GenerationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGenerationException(GenerationException e) {
        log.error("Ошибка генерации: {}", e.getMessage(), e);
        return new ErrorResponse(e.getMessage(), Instant.now(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(MethodArgumentNotValidException e) {
        List<String> errors = new ArrayList<>();
        e.getBindingResult().getFieldErrors().forEach(s -> errors.add("Ошибка поля " + s.getField() + ": " + s.getDefaultMessage()));
        log.warn("Ошибка валидации полей: {}", errors);
        return new ErrorResponse("Ошибка валидации полей", Instant.now(), errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolation(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> errors = e.getConstraintViolations();
        List<String> result = new ArrayList<>();
        errors.forEach(s -> result.add("Ошибка поля " + s.getPropertyPath() + ": " + s.getMessage()));
        log.warn("Ошибка валидации полей: {}", result);
        return new ErrorResponse("Ошибка валидации полей", Instant.now(), result);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleServerError(Exception e) {
        log.error("Ошибка кода.", e);
        return new ErrorResponse("Ошибка сервера", Instant.now(), null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleHttpParsing(HttpMessageNotReadableException e) {
        log.warn(e.getMessage());
        return new ErrorResponse("Ошибка парсинга строки", Instant.now(), null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleEnumMismatch(MethodArgumentTypeMismatchException e) {
        log.warn(e.getMessage());
        return new ErrorResponse("Ошибка валидации данных, проверьте правописание WEEKLY", Instant.now(), null);
    }
}
