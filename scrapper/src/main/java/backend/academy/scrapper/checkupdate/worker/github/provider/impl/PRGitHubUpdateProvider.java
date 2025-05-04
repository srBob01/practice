package backend.academy.scrapper.checkupdate.worker.github.provider.impl;

import backend.academy.scrapper.checkupdate.worker.github.provider.AbstractGitHubUpdateProvider;
import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.converter.base.impl.GitHubResponseConverter;
import backend.academy.scrapper.model.app.request.GitHubLinkRequest;
import backend.academy.scrapper.model.app.response.githib.GitHubCommentResponse;
import backend.academy.scrapper.model.app.response.githib.GitHubCommitResponse;
import backend.academy.scrapper.model.app.response.githib.GitHubResponse;
import backend.academy.scrapper.model.app.update.impl.GitHubUpdateDetail;
import backend.academy.scrapper.model.db.link.impl.type.GitHubEventType;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Провайдер обновлений для Pull Request GitHub. Сравнивает время последнего комментария и последнего коммита, чтобы
 * определить, какое событие произошло позже.
 */
@Component
public class PRGitHubUpdateProvider extends AbstractGitHubUpdateProvider {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private final GitHubResponseConverter responseConverter;

    public PRGitHubUpdateProvider(
            RestClient restClient, ScrapperConfig scrapperConfig, GitHubResponseConverter responseConverter) {
        super(restClient, scrapperConfig);
        this.responseConverter = responseConverter;
    }

    /**
     * Возвращает поддерживаемый тип события (PR).
     *
     * @return GitHubEventType.PR
     */
    @Override
    public GitHubEventType getSupportedType() {
        return GitHubEventType.PR;
    }

    /**
     * Обрабатывает обновление для PR. Сравнивает время последнего комментария и последнего коммита, чтобы определить,
     * какое событие произошло позже, и формирует соответствующее сообщение об обновлении.
     *
     * @param request объект GitHubLinkRequest с данными PR
     * @return GitHubUpdateDetail с информацией об обновлении
     */
    @Override
    public GitHubUpdateDetail processUpdate(GitHubLinkRequest request) {
        GitHubResponse baseResponse = fetchBaseResponse(request);
        String owner = request.owner();
        String repo = request.repo();
        String prNumber = request.itemNumber();

        GitHubCommentResponse comment = fetchLatestPRCommentPaginated(owner, repo, prNumber);
        GitHubCommitResponse commit = fetchLatestPRCommitPaginated(owner, repo, prNumber);

        LocalDateTime commentTime = (comment != null) ? LocalDateTime.parse(comment.createdAt(), DATE_FORMATTER) : null;
        LocalDateTime commitTime =
                (commit != null && commit.commit() != null && commit.commit().author() != null)
                        ? LocalDateTime.parse(commit.commit().author().date(), DATE_FORMATTER)
                        : null;

        if (commentTime != null && (commitTime == null || commentTime.isAfter(commitTime))) {
            return createUpdateDetailForPRComment(baseResponse, comment, commentTime);
        } else if (commitTime != null && (commentTime == null || commitTime.isAfter(commentTime))) {
            return createUpdateDetailForPRCommit(baseResponse, commit, commitTime);
        } else {
            return responseConverter.convert(baseResponse);
        }
    }

    /**
     * Формирует детали обновления для нового комментария в PR.
     *
     * @param baseResponse базовый ответ PR
     * @param comment объект комментария
     * @param commentTime время создания комментария
     * @return GitHubUpdateDetail для комментария
     */
    private GitHubUpdateDetail createUpdateDetailForPRComment(
            GitHubResponse baseResponse, GitHubCommentResponse comment, LocalDateTime commentTime) {
        String detail = String.format(
                "New comment on PR '%s' by %s at %s. Comment: %s",
                baseResponse.title(), comment.user().login(), commentTime, truncate(comment.body(), 200));
        return new GitHubUpdateDetail(baseResponse.title(), baseResponse.user().login(), commentTime, detail);
    }

    /**
     * Формирует детали обновления для нового коммита в PR.
     *
     * @param baseResponse базовый ответ PR
     * @param commit объект коммита
     * @param commitTime время создания коммита
     * @return GitHubUpdateDetail для коммита (с указанием commit ID)
     */
    private GitHubUpdateDetail createUpdateDetailForPRCommit(
            GitHubResponse baseResponse, GitHubCommitResponse commit, LocalDateTime commitTime) {
        String detail = String.format(
                "New commit in PR '%s' by %s at %s. Commit message: %s (Commit ID: %s)",
                baseResponse.title(),
                commit.commit().author().name(),
                commitTime,
                truncate(commit.commit().message(), 200),
                commit.sha());
        return new GitHubUpdateDetail(baseResponse.title(), baseResponse.user().login(), commitTime, detail);
    }
}
