package backend.academy.bot.validator;

/** Интерфейс для проверки корректности ссылки. */
public interface LinkValidator {
    /**
     * Проверяет, является ли переданный URL корректной ссылкой.
     *
     * @param url URL для проверки
     * @return true, если ссылка корректна, иначе false
     */
    boolean isValidLink(String url);

    /**
     * Возвращает приоритет данного валидатора.
     *
     * @return приоритет валидатора
     */
    ValidatorPriority getPriority();
}
