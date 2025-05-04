package backend.academy.bot.exception.handler;

import backend.academy.bot.exception.model.BotException;
import backend.academy.dto.ApiErrorResponse;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Глобальный обработчик исключений для REST-контроллеров бота.
 *
 * <p>Перехватывает различные исключения и возвращает объект {@link ApiErrorResponse} с детальной информацией об ошибке.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обрабатывает все исключения типа {@link BotException}.
     *
     * @param ex экземпляр BotException с подробностями ошибки
     * @return тело ответа с кодом и сообщением об ошибке
     */
    @ExceptionHandler(BotException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleBotException(BotException ex) {
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
     * Обрабатывает ошибки валидации входных параметров ({@link MethodArgumentNotValidException}).
     *
     * @param ex исключение, содержащее ошибки валидации полей
     * @return ответ с перечнем полей и сообщениями об ошибках
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
     * Обрабатывает все прочие непредвиденные исключения.
     *
     * @param ex любой непойманный ранее Exception
     * @return ответ с информацией об ошибке и стек-трейсом
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
