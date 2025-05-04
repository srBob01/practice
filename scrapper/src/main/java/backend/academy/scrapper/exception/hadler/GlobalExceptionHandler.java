package backend.academy.scrapper.exception.hadler;

import backend.academy.dto.ApiErrorResponse;
import backend.academy.scrapper.exception.model.ScrapperException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Глобальный обработчик исключений для REST-контроллеров Scrapper.
 *
 * <p>Перехватывает специфичные и общие исключения, формируя единый формат ответа {@link ApiErrorResponse} с деталями
 * ошибки и, при необходимости, стек-трейсом.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обрабатывает пользовательские исключения ScrapperException.
     *
     * @param ex экземпляр {@link ScrapperException}
     * @return сформированный объект {@link ApiErrorResponse}
     */
    @ExceptionHandler(ScrapperException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleBotException(ScrapperException ex) {
        List<String> stackTrace = Arrays.stream(ex.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.toList());
        return new ApiErrorResponse(
                "Bot exception occurred",
                String.valueOf(HttpStatus.BAD_REQUEST.value()),
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                stackTrace);
    }

    /**
     * Обрабатывает ошибки валидации полей запроса ({@link MethodArgumentNotValidException}).
     *
     * @param ex исключение валидации
     * @return ответ с перечнем ошибок валидации
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleValidationException(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return new ApiErrorResponse(
                "Validation error",
                String.valueOf(HttpStatus.BAD_REQUEST.value()),
                ex.getClass().getSimpleName(),
                errors,
                null);
    }

    /**
     * Обрабатывает ошибки несовпадения типов аргументов запроса ({@link MethodArgumentTypeMismatchException}).
     *
     * @param ex исключение неверного типа параметра
     * @return ответ с информацией о некорректном параметре
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return new ApiErrorResponse(
                "Validation error",
                String.valueOf(HttpStatus.BAD_REQUEST.value()),
                ex.getClass().getSimpleName(),
                "Invalid parameter: " + ex.getValue(),
                null);
    }

    /**
     * Обрабатывает отсутствие обязательного заголовка запроса.
     *
     * @param ex исключение отсутствия заголовка
     * @return ответ с указанием отсутствующего заголовка
     */
    @ExceptionHandler(org.springframework.web.bind.MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleMissingRequestHeaderException(
            org.springframework.web.bind.MissingRequestHeaderException ex) {
        return new ApiErrorResponse(
                "Validation error",
                String.valueOf(HttpStatus.BAD_REQUEST.value()),
                ex.getClass().getSimpleName(),
                "Missing header: " + ex.getHeaderName(),
                null);
    }

    /**
     * Обрабатывает все прочие непредвиденные исключения.
     *
     * @param ex любое необработанное исключение
     * @return ответ с деталями ошибки и стек-трейсом
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleGeneralException(Exception ex) {
        List<String> stackTrace = Arrays.stream(ex.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.toList());
        return new ApiErrorResponse(
                "An unexpected error occurred",
                String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                stackTrace);
    }
}
