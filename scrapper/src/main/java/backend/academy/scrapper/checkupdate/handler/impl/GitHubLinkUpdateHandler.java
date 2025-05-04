package backend.academy.scrapper.checkupdate.handler.impl;

import backend.academy.scrapper.checkupdate.handler.LinkUpdateHandler;
import backend.academy.scrapper.checkupdate.worker.github.GitHubUpdateService;
import backend.academy.scrapper.converter.base.impl.GitHubLinkRequestConverter;
import backend.academy.scrapper.model.app.update.UpdateDetail;
import backend.academy.scrapper.model.db.link.LinkType;
import backend.academy.scrapper.model.db.link.impl.GitHubLink;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GitHubLinkUpdateHandler implements LinkUpdateHandler<GitHubLink> {

    private final GitHubUpdateService gitHubUpdateService;
    private final GitHubLinkRequestConverter requestConverter;

    @Override
    public LinkType getSupportedType() {
        return LinkType.GITHUB;
    }

    @Override
    public UpdateDetail fetchUpdateDetail(GitHubLink link) {
        var request = requestConverter.convert(link);
        assert request != null;
        return gitHubUpdateService.fetchLatestUpdateDetail(request);
    }
}
