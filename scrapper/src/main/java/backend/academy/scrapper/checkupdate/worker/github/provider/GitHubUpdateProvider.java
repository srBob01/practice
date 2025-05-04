package backend.academy.scrapper.checkupdate.worker.github.provider;

import backend.academy.scrapper.model.app.request.GitHubLinkRequest;
import backend.academy.scrapper.model.app.update.impl.GitHubUpdateDetail;
import backend.academy.scrapper.model.db.link.impl.type.GitHubEventType;

/** Интерфейс для обработки обновлений GitHub. */
public interface GitHubUpdateProvider {
    /**
     * Возвращает тип события, поддерживаемый данным провайдером.
     *
     * @return поддерживаемый тип события GitHub
     */
    GitHubEventType getSupportedType();

    /**
     * Обрабатывает обновление по заданному запросу и возвращает детали обновления.
     *
     * @param request объект запроса GitHubLinkRequest
     * @return детали обновления GitHubUpdateDetail
     */
    GitHubUpdateDetail processUpdate(GitHubLinkRequest request);
}
