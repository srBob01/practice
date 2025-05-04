package backend.academy.scrapper.model.app.request;

import backend.academy.scrapper.model.db.link.impl.type.GitHubEventType;

/**
 * Объект, инкапсулирующий параметры для запроса обновлений GitHub. Теперь включает дополнительное поле branch (для
 * репозиториев).
 */
public record GitHubLinkRequest(String owner, String repo, String itemNumber, GitHubEventType eventType) {}
