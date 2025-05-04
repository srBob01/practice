package backend.academy.scrapper.converter.base.impl;

import backend.academy.scrapper.model.app.response.githib.GitHubResponse;
import backend.academy.scrapper.model.app.update.impl.GitHubUpdateDetail;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class GitHubResponseConverter implements Converter<GitHubResponse, GitHubUpdateDetail> {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @Override
    public GitHubUpdateDetail convert(@NotNull GitHubResponse response) {
        String timeStr = response.updatedAt() != null ? response.updatedAt() : response.createdAt();
        LocalDateTime activityTime = LocalDateTime.parse(timeStr, DATE_FORMATTER);
        String title = response.title();
        String username = response.user().login();
        String body = response.body() != null ? response.body() : "";
        String descriptionPreview = body.length() > 200 ? body.substring(0, 200) : body;
        return new GitHubUpdateDetail(title, username, activityTime, descriptionPreview);
    }
}
