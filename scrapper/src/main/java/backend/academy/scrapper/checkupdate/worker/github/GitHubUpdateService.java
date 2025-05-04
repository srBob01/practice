package backend.academy.scrapper.checkupdate.worker.github;

import backend.academy.scrapper.checkupdate.worker.github.provider.GitHubUpdateProvider;
import backend.academy.scrapper.model.app.request.GitHubLinkRequest;
import backend.academy.scrapper.model.app.update.impl.GitHubUpdateDetail;
import backend.academy.scrapper.model.db.link.impl.type.GitHubEventType;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GitHubUpdateService {

    private final Map<GitHubEventType, GitHubUpdateProvider> providerMap;

    public GitHubUpdateDetail fetchLatestUpdateDetail(GitHubLinkRequest request) {
        GitHubUpdateProvider provider = providerMap.get(request.eventType());
        if (provider == null) {
            throw new IllegalArgumentException("No provider for event type: " + request.eventType());
        }
        return provider.processUpdate(request);
    }
}
