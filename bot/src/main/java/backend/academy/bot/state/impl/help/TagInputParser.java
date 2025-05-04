package backend.academy.bot.state.impl.help;

import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class TagInputParser {

    /**
     * Разбивает ввод пользователя вида "https://example.com:tag" на ссылку и тег.
     *
     * @param input строка, содержащая ссылку и тег, разделённые последним двоеточием
     * @return Optional из [link, tag], если формат валиден, иначе пустой
     */
    public Optional<LinkTagPair> parse(String input) {
        if (input == null || input.isBlank()) return Optional.empty();

        int splitIndex = input.lastIndexOf(':');
        if (splitIndex <= 0 || splitIndex == input.length() - 1) return Optional.empty();

        String link = input.substring(0, splitIndex).trim();
        String tag = input.substring(splitIndex + 1).trim();

        if (link.isEmpty() || tag.isEmpty()) return Optional.empty();

        return Optional.of(new LinkTagPair(link, tag));
    }

    /** Вспомогательная структура данных. */
    public record LinkTagPair(String link, String tag) {}
}
