package backend.academy.scrapper.checkupdate.worker.github.provider.impl;

import backend.academy.scrapper.checkupdate.worker.github.provider.AbstractGitHubUpdateProvider;
import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.converter.base.impl.GitHubResponseConverter;
import backend.academy.scrapper.model.app.request.GitHubLinkRequest;
import backend.academy.scrapper.model.app.response.githib.GitHubCommentResponse;
import backend.academy.scrapper.model.app.response.githib.GitHubResponse;
import backend.academy.scrapper.model.app.update.impl.GitHubUpdateDetail;
import backend.academy.scrapper.model.db.link.impl.type.GitHubEventType;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Провайдер обновлений для Issue GitHub. Если время обновления отличается от времени создания, считается, что появилось
 * новое обновление (например, комментарий).
 */
@Component
public class IssueGitHubUpdateProvider extends AbstractGitHubUpdateProvider {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private final GitHubResponseConverter responseConverter;

    public IssueGitHubUpdateProvider(
            RestClient restClient, ScrapperConfig scrapperConfig, GitHubResponseConverter responseConverter) {
        super(restClient, scrapperConfig);
        this.responseConverter = responseConverter;
    }

    @Override
    public GitHubEventType getSupportedType() {
        return GitHubEventType.ISSUE;
    }

    /**
     * Обрабатывает обновление для Issue. Если время обновления отличается от времени создания, выбирается последнее
     * событие (например, комментарий).
     *
     * @param request объект GitHubLinkRequest с данными Issue
     * @return GitHubUpdateDetail с информацией об обновлении
     */
    @Override
    public GitHubUpdateDetail processUpdate(GitHubLinkRequest request) {
        GitHubResponse baseResponse = fetchBaseResponse(request);
        if (baseResponse.updatedAt() != null && !Objects.equals(baseResponse.updatedAt(), baseResponse.createdAt())) {
            GitHubCommentResponse comment =
                    fetchLatestIssueCommentPaginated(request.owner(), request.repo(), request.itemNumber());
            if (comment != null) {
                return getGitHubUpdateDetail(comment, baseResponse);
            }
        }
        return responseConverter.convert(baseResponse);
    }

    /**
     * Формирует детали обновления для нового коммита в PR.
     *
     * @param baseResponse базовый ответ PR
     * @param comment объект комментария
     * @return GitHubUpdateDetail для коммита (с указанием commit ID)
     */
    private @NotNull GitHubUpdateDetail getGitHubUpdateDetail(
            GitHubCommentResponse comment, GitHubResponse baseResponse) {
        LocalDateTime commentTime = LocalDateTime.parse(comment.createdAt(), DATE_FORMATTER);
        String detailDesc = String.format(
                "New comment on issue '%s' by %s at %s. Comment: %s",
                baseResponse.title(), comment.user().login(), commentTime, truncate(comment.body(), 200));
        return new GitHubUpdateDetail(baseResponse.title(), baseResponse.user().login(), commentTime, detailDesc);
    }
}
