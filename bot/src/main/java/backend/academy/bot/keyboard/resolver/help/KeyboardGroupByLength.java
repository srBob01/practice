package backend.academy.bot.keyboard.resolver.help;

import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/** Утилитарный компонент для группировки кнопок клавиатуры по общей длине строк. */
@Component
public class KeyboardGroupByLength {

    /**
     * Группирует список строк-команд в ряды кнопок Telegram-клавиатуры.
     *
     * @param commands список текстовых команд для создания кнопок
     * @param maxLengthPerRow максимальная суммарная длина текста кнопок в одном ряду
     * @return объект {@link ReplyKeyboardMarkup} с организованными рядами кнопок, установленным ресайзом клавиатуры и
     *     однократным показом
     */
    public ReplyKeyboardMarkup groupButtonsByLength(List<String> commands, int maxLengthPerRow) {
        List<List<KeyboardButton>> rows = new ArrayList<>();
        List<KeyboardButton> currentRow = new ArrayList<>();
        int currentLength = 0;

        for (String command : commands) {
            int length = command.length();
            if (currentLength + length > maxLengthPerRow && !currentRow.isEmpty()) {
                rows.add(currentRow);
                currentRow = new ArrayList<>();
                currentLength = 0;
            }
            currentRow.add(new KeyboardButton(command));
            currentLength += length + 1;
        }

        if (!currentRow.isEmpty()) {
            rows.add(currentRow);
        }

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(
                rows.stream().map(row -> row.toArray(new KeyboardButton[0])).toArray(KeyboardButton[][]::new));
        return keyboard.resizeKeyboard(true).oneTimeKeyboard(true);
    }
}
