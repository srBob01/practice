package backend.academy.bot.service.validator;

import backend.academy.bot.validator.LinkValidator;
import backend.academy.bot.validator.ValidatorPriority;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Сервис валидации ссылок.
 *
 * <p>Использует список валидаторов {@link LinkValidator}, чтобы проверить корректность URL в порядке возрастания
 * приоритета. Приоритет валидатора определяется через {@link ValidatorPriority}.
 */
@Service
public class LinkValidationService {

    private final List<LinkValidator> linkValidators;
    private final Integer costMaxPriority;

    /**
     * Конструктор, внедряющий список кастомных валидаторов и максимальный приоритет.
     *
     * @param customLinkValidators список валидаторов (с квалификатором "customLinkValidators")
     * @param maxCostPriority максимальный приоритет, до которого будут применяться валидаторы
     */
    public LinkValidationService(List<LinkValidator> customLinkValidators, Integer maxCostPriority) {
        this.linkValidators = customLinkValidators;
        this.costMaxPriority = maxCostPriority;
    }

    /**
     * Проверяет корректность URL, используя валидаторы с приоритетом не выше указанного.
     *
     * @param url URL для проверки
     * @param cost порог приоритета (стоимость)
     * @return true, если все валидаторы с приоритетом <= cost возвращают true, иначе false
     */
    public boolean validateLink(String url, Integer cost) {
        return linkValidators.stream()
                .takeWhile(validator -> validator.getPriority().cost() <= cost)
                .allMatch(validator -> validator.isValidLink(url));
    }

    /**
     * Проверяет корректность URL, используя максимальный приоритет, заданный в конфигурации.
     *
     * @param url URL для проверки
     * @return true, если ссылка корректна, иначе false
     */
    public boolean validateLink(String url) {
        return validateLink(url, costMaxPriority);
    }
}
