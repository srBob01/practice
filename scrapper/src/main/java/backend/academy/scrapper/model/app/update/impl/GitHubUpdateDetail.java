package backend.academy.scrapper.model.app.update.impl;

import backend.academy.scrapper.model.app.update.UpdateDetail;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GitHubUpdateDetail implements UpdateDetail {
    private final String title;
    private final String username;
    private final LocalDateTime creationTime;
    private final String descriptionPreview;

    @Override
    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    @Override
    public String getDescription() {
        return String.format("GitHub %s by %s. Preview: %s", title, username, descriptionPreview);
    }
}
