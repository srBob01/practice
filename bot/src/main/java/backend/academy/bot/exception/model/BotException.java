package backend.academy.bot.exception.model;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
@Getter
public class BotException extends RuntimeException {
    private final String description;

    public BotException(String message) {
        super(message);
        this.description = message;
    }
}
