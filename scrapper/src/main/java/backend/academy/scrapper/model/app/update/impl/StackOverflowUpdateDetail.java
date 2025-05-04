package backend.academy.scrapper.model.app.update.impl;

import backend.academy.scrapper.model.app.update.UpdateDetail;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StackOverflowUpdateDetail implements UpdateDetail {
    private final String questionTitle;
    private final String username;
    private final LocalDateTime creationTime;
    private final String preview;

    @Override
    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    @Override
    public String getDescription() {
        return String.format("SO question '%s' answered by %s. Preview: %s", questionTitle, username, preview);
    }
}
