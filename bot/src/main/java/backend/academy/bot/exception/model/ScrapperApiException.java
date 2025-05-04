package backend.academy.bot.exception.model;

import backend.academy.dto.ApiErrorResponse;
import lombok.Getter;

@Getter
public class ScrapperApiException extends RuntimeException {
    private final ApiErrorResponse errorResponse;

    /**
     * Создает новое исключение на основе полученного ApiErrorResponse.
     *
     * @param errorResponse объект с подробностями ошибки, полученной от Bot API
     */
    public ScrapperApiException(ApiErrorResponse errorResponse) {
        super(errorResponse.description());
        this.errorResponse = errorResponse;
    }
}
