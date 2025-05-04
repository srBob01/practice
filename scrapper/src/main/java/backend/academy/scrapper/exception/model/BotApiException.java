package backend.academy.scrapper.exception.model;

import backend.academy.dto.ApiErrorResponse;
import lombok.Getter;

/**
 * Исключение, выбрасываемое при ошибках, возвращаемых Bot API. Содержит подробную информацию об ошибке для детального
 * логирования и обработки.
 */
@Getter
public class BotApiException extends RuntimeException {
    private final ApiErrorResponse errorResponse;

    /**
     * Создает новое исключение на основе полученного ApiErrorResponse.
     *
     * @param errorResponse объект с подробностями ошибки, полученной от Bot API
     */
    public BotApiException(ApiErrorResponse errorResponse) {
        super(errorResponse.description());
        this.errorResponse = errorResponse;
    }
}
