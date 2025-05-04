package backend.academy.scrapper.checkupdate.worker.github.provider;

import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.model.app.request.GitHubLinkRequest;
import backend.academy.scrapper.model.app.response.githib.GitHubCommentResponse;
import backend.academy.scrapper.model.app.response.githib.GitHubCommitResponse;
import backend.academy.scrapper.model.app.response.githib.GitHubResponse;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

/**
 * Абстрактный провайдер обновлений GitHub, содержащий общие методы для выполнения HTTP-запросов, обработки пагинации и
 * формирования URI по заданным шаблонам.
 */
public abstract class AbstractGitHubUpdateProvider implements GitHubUpdateProvider {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    protected final RestClient restClient;
    protected final String githubToken;

    public AbstractGitHubUpdateProvider(RestClient restClient, ScrapperConfig scrapperConfig) {
        this.restClient = restClient;
        this.githubToken = scrapperConfig.githubToken();
    }

    /**
     * Выполняет HTTP GET-запрос по заданному URI и возвращает тело ответа в виде массива объектов указанного класса.
     *
     * @param uri URI запроса
     * @param clazz Класс массива объектов, который требуется вернуть
     * @param <T> Тип объектов
     * @return Массив объектов, полученных в ответе, или null, если ответ отсутствует
     */
    protected <T> T[] executeGet(String uri, Class<T[]> clazz) {
        return restClient
                .get()
                .uri(uri)
                .header(AUTHORIZATION_HEADER, "token " + githubToken)
                .retrieve()
                .body(clazz);
    }

    /**
     * Получает базовый ответ GitHub по заданному запросу. Формирует URI с использованием шаблона, определённого в
     * GitHubEventType, и данных запроса.
     *
     * @param request Объект запроса GitHubLinkRequest
     * @return Объект GitHubResponse, полученный от GitHub API
     */
    protected GitHubResponse fetchBaseResponse(GitHubLinkRequest request) {
        String endpoint = request.eventType().endpointPattern();
        String uri = String.format(endpoint, request.owner(), request.repo(), request.itemNumber());
        return restClient
                .get()
                .uri(uri)
                .header(AUTHORIZATION_HEADER, "token " + githubToken)
                .retrieve()
                .body(GitHubResponse.class);
    }

    /**
     * Универсальный метод для получения последнего элемента через механизм пагинации. Формируется запрос с параметром
     * per_page=1, анализируется заголовок Link и, если возможно, производится переход на последнюю страницу.
     *
     * @param baseUrl Базовый URL запроса
     * @param clazz Класс массива ответа
     * @param <T> Тип объектов ответа
     * @return Последний элемент, полученный через пагинацию, или null, если данные отсутствуют
     */
    protected <T> T fetchLatestPaginated(String baseUrl, Class<T[]> clazz) {
        String url = baseUrl + (baseUrl.contains("?") ? "&per_page=1" : "?per_page=1");
        ResponseEntity<T[]> initialResponse = restClient
                .get()
                .uri(url)
                .header(AUTHORIZATION_HEADER, "token " + githubToken)
                .retrieve()
                .toEntity(clazz);
        HttpHeaders headers = initialResponse.getHeaders();
        List<String> linkHeader = headers.get("Link");
        if (linkHeader == null || linkHeader.isEmpty()) {
            T[] body = initialResponse.getBody();
            return (body != null && body.length > 0) ? body[0] : null;
        }
        String lastPageUrl = parseLastPageUrl(linkHeader.get(0));
        if (lastPageUrl == null) {
            T[] body = initialResponse.getBody();
            return (body != null && body.length > 0) ? body[0] : null;
        }
        T[] lastPageItems = restClient
                .get()
                .uri(lastPageUrl)
                .header(AUTHORIZATION_HEADER, "token " + githubToken)
                .retrieve()
                .body(clazz);
        return (lastPageItems != null && lastPageItems.length > 0) ? lastPageItems[0] : null;
    }

    /**
     * Формирует URI на основе заданного шаблона и аргументов, а затем получает последний элемент через механизм
     * пагинации.
     *
     * @param uriPattern Шаблон URI с позиционными спецификаторами
     * @param clazz Класс массива ответа
     * @param args Аргументы для шаблона
     * @param <T> Тип объектов ответа
     * @return Последний элемент, полученный через пагинацию, или null, если данные отсутствуют
     */
    @SuppressWarnings("AnnotateFormatMethod")
    protected <T> T fetchLatestByPattern(String uriPattern, Class<T[]> clazz, Object... args) {
        String baseUrl = String.format(uriPattern, args);
        return fetchLatestPaginated(baseUrl, clazz);
    }

    /**
     * Получает последний комментарий для Issue.
     *
     * @param owner Имя владельца репозитория
     * @param repo Имя репозитория
     * @param issueNumber Номер Issue
     * @return Объект GitHubCommentResponse или null, если комментариев нет
     */
    protected GitHubCommentResponse fetchLatestIssueCommentPaginated(String owner, String repo, String issueNumber) {
        String pattern = "https://api.github.com/repos/%s/%s/issues/%s/comments";
        return fetchLatestByPattern(pattern, GitHubCommentResponse[].class, owner, repo, issueNumber);
    }

    /**
     * Получает последний комментарий для PR.
     *
     * @param owner Имя владельца репозитория
     * @param repo Имя репозитория
     * @param prNumber Номер PR
     * @return Объект GitHubCommentResponse или null, если комментариев нет
     */
    protected GitHubCommentResponse fetchLatestPRCommentPaginated(String owner, String repo, String prNumber) {
        String pattern = "https://api.github.com/repos/%s/%s/issues/%s/comments";
        return fetchLatestByPattern(pattern, GitHubCommentResponse[].class, owner, repo, prNumber);
    }

    /**
     * Получает последний коммит для PR через механизм пагинации. Для PR-коммитов GitHub возвращает данные в порядке
     * возрастания, поэтому используется пагинация для выбора последнего элемента.
     *
     * @param owner Имя владельца репозитория
     * @param repo Имя репозитория
     * @param prNumber Номер PR
     * @return Объект GitHubCommitResponse или null, если коммитов нет
     */
    protected GitHubCommitResponse fetchLatestPRCommitPaginated(String owner, String repo, String prNumber) {
        String pattern = "https://api.github.com/repos/%s/%s/pulls/%s/commits";
        return fetchLatestByPattern(pattern, GitHubCommitResponse[].class, owner, repo, prNumber);
    }

    /**
     * Получает последний коммит для репозитория с использованием прямого запроса (без пагинации). GitHub возвращает
     * коммиты в порядке убывания (новейший первый).
     *
     * @param owner Имя владельца репозитория
     * @param repo Имя репозитория
     * @return Объект GitHubCommitResponse или null, если коммитов нет
     */
    protected GitHubCommitResponse fetchLatestRepoCommitDirect(String owner, String repo) {
        String uri = String.format("https://api.github.com/repos/%s/%s/commits?per_page=1", owner, repo);
        GitHubCommitResponse[] responses = executeGet(uri, GitHubCommitResponse[].class);
        return (responses != null && responses.length > 0) ? responses[0] : null;
    }

    /**
     * Получает последний созданный PR для репозитория с сортировкой по дате создания (по убыванию).
     *
     * @param owner Имя владельца репозитория
     * @param repo Имя репозитория
     * @return Объект GitHubResponse или null, если PR отсутствуют
     */
    protected GitHubResponse fetchLatestPRCreation(String owner, String repo) {
        String uri = String.format(
                "https://api.github.com/repos/%s/%s/pulls?sort=created&direction=desc&per_page=1", owner, repo);
        GitHubResponse[] responses = executeGet(uri, GitHubResponse[].class);
        return (responses != null && responses.length > 0) ? responses[0] : null;
    }

    /**
     * Получает последний созданный Issue для репозитория с сортировкой по дате создания (по убыванию).
     *
     * @param owner Имя владельца репозитория
     * @param repo Имя репозитория
     * @return Объект GitHubResponse или null, если Issue отсутствуют
     */
    protected GitHubResponse fetchLatestIssueCreation(String owner, String repo) {
        String uri = String.format(
                "https://api.github.com/repos/%s/%s/issues?sort=created&direction=desc&per_page=1", owner, repo);
        GitHubResponse[] responses = executeGet(uri, GitHubResponse[].class);
        return (responses != null && responses.length > 0) ? responses[0] : null;
    }

    /**
     * Парсит заголовок Link для извлечения URL последней страницы.
     *
     * @param linkHeader Строка заголовка Link
     * @return URL последней страницы или null, если не найден
     */
    @SuppressWarnings("StringSplitter")
    protected String parseLastPageUrl(String linkHeader) {
        String[] parts = linkHeader.split(",");
        for (String part : parts) {
            if (part.contains("rel=\"last\"")) {
                int start = part.indexOf("<") + 1;
                int end = part.indexOf(">");
                if (start > 0 && end > start) {
                    return part.substring(start, end);
                }
            }
        }
        return null;
    }

    /**
     * Обрезает текст до указанного максимального количества символов.
     *
     * @param text Исходный текст
     * @param maxLength Максимальная длина
     * @return Обрезанный текст или исходный, если его длина не превышает maxLength
     */
    protected String truncate(String text, int maxLength) {
        if (text == null) {
            return null;
        }
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
    }
}
