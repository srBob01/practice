package backend.academy.scrapper.exception.model;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
@Getter
public class ScrapperException extends RuntimeException {
    private final String description;

    public ScrapperException(String description, String message) {
        super(message);
        this.description = description;
    }
}
