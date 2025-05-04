package backend.academy.scrapper.checkupdate.worker.github.provider.impl;

import backend.academy.scrapper.checkupdate.worker.github.provider.AbstractGitHubUpdateProvider;
import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.converter.base.impl.GitHubResponseConverter;
import backend.academy.scrapper.model.app.request.GitHubLinkRequest;
import backend.academy.scrapper.model.app.response.githib.GitHubCommitResponse;
import backend.academy.scrapper.model.app.response.githib.GitHubResponse;
import backend.academy.scrapper.model.app.update.impl.GitHubUpdateDetail;
import backend.academy.scrapper.model.db.link.impl.type.GitHubEventType;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Провайдер обновлений для репозитория GitHub. Сравнивает последние события: коммит, создание PR и Issue, и возвращает
 * детальную информацию о самом свежем обновлении.
 */
@Component
public class RepoGitHubUpdateProvider extends AbstractGitHubUpdateProvider {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private final GitHubResponseConverter responseConverter;

    public RepoGitHubUpdateProvider(
            RestClient restClient, ScrapperConfig scrapperConfig, GitHubResponseConverter responseConverter) {
        super(restClient, scrapperConfig);
        this.responseConverter = responseConverter;
    }

    /**
     * Возвращает поддерживаемый тип события (REPO).
     *
     * @return GitHubEventType.REPO
     */
    @Override
    public GitHubEventType getSupportedType() {
        return GitHubEventType.REPO;
    }

    /**
     * Обрабатывает обновление репозитория, сравнивая время последнего коммита, создания PR и Issue. Если ни одно
     * обновление не найдено, возвращается базовый ответ.
     *
     * @param request Объект запроса GitHubLinkRequest с данными репозитория
     * @return Детали обновления GitHubUpdateDetail
     */
    @Override
    public GitHubUpdateDetail processUpdate(GitHubLinkRequest request) {
        String owner = request.owner();
        String repo = request.repo();

        GitHubCommitResponse commit = fetchLatestRepoCommitDirect(owner, repo);
        GitHubResponse prResponse = fetchLatestPRCreation(owner, repo);
        GitHubResponse issueResponse = fetchLatestIssueCreation(owner, repo);

        LocalDateTime commitTime = extractCommitTime(commit);
        LocalDateTime prTime = extractTime(prResponse != null ? prResponse.createdAt() : null);
        LocalDateTime issueTime = extractTime(issueResponse != null ? issueResponse.createdAt() : null);

        GitHubUpdateDetail updateDetail = null;
        LocalDateTime latestTime = null;

        if (commitTime != null) {
            latestTime = commitTime;
            updateDetail = createUpdateDetailForRepoCommit(repo, commit, commitTime);
        }
        if (prTime != null && (latestTime == null || prTime.isAfter(latestTime))) {
            latestTime = prTime;
            updateDetail = createUpdateDetailForRepoPR(repo, prResponse, prTime);
        }
        if (issueTime != null && (latestTime == null || issueTime.isAfter(latestTime))) {
            latestTime = issueTime;
            updateDetail = createUpdateDetailForRepoIssue(repo, issueResponse, issueTime);
        }

        if (latestTime == null) {
            // Если ни одно событие не найдено, возвращаем базовый ответ
            GitHubResponse baseResponse = fetchBaseResponse(request);
            return responseConverter.convert(baseResponse);
        }
        return updateDetail;
    }

    /**
     * Извлекает время коммита из объекта GitHubCommitResponse.
     *
     * @param commit Объект коммита
     * @return Время коммита или null, если информация отсутствует
     */
    private LocalDateTime extractCommitTime(GitHubCommitResponse commit) {
        if (commit != null && commit.commit() != null && commit.commit().author() != null) {
            return LocalDateTime.parse(commit.commit().author().date(), DATE_FORMATTER);
        }
        return null;
    }

    /**
     * Преобразует строку времени в LocalDateTime.
     *
     * @param timeStr Строка времени в формате ISO
     * @return LocalDateTime или null, если строка пуста
     */
    private LocalDateTime extractTime(String timeStr) {
        if (timeStr != null && !timeStr.isEmpty()) {
            return LocalDateTime.parse(timeStr, DATE_FORMATTER);
        }
        return null;
    }

    /**
     * Формирует детали обновления для нового коммита в репозитории.
     *
     * @param repo Имя репозитория
     * @param commit Объект коммита
     * @param commitTime Время коммита
     * @return GitHubUpdateDetail с информацией о коммите
     */
    private GitHubUpdateDetail createUpdateDetailForRepoCommit(
            String repo, GitHubCommitResponse commit, LocalDateTime commitTime) {
        String detail = String.format(
                "New commit in repository '%s' by %s at %s. Commit message: %s (Commit ID: %s)",
                repo,
                commit.commit().author().name(),
                commitTime,
                truncate(commit.commit().message(), 200),
                commit.sha());
        return new GitHubUpdateDetail(repo, commit.commit().author().name(), commitTime, detail);
    }

    /**
     * Формирует детали обновления для нового PR в репозитории.
     *
     * @param repo Имя репозитория
     * @param prResponse Объект PR
     * @param prTime Время создания PR
     * @return GitHubUpdateDetail с информацией о PR
     */
    private GitHubUpdateDetail createUpdateDetailForRepoPR(
            String repo, GitHubResponse prResponse, LocalDateTime prTime) {
        String detail = String.format(
                "New PR in repository '%s': '%s' by %s at %s. Description: %s",
                repo, prResponse.title(), prResponse.user().login(), prTime, truncate(prResponse.body(), 200));
        return new GitHubUpdateDetail(repo, prResponse.user().login(), prTime, detail);
    }

    /**
     * Формирует детали обновления для нового Issue в репозитории.
     *
     * @param repo Имя репозитория
     * @param issueResponse Объект Issue
     * @param issueTime Время создания Issue
     * @return GitHubUpdateDetail с информацией об Issue
     */
    private GitHubUpdateDetail createUpdateDetailForRepoIssue(
            String repo, GitHubResponse issueResponse, LocalDateTime issueTime) {
        String detail = String.format(
                "New issue in repository '%s': '%s' by %s at %s. Description: %s",
                repo,
                issueResponse.title(),
                issueResponse.user().login(),
                issueTime,
                truncate(issueResponse.body(), 200));
        return new GitHubUpdateDetail(repo, issueResponse.user().login(), issueTime, detail);
    }
}
