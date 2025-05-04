package backend.academy.scrapper.converter.base.impl;

import backend.academy.scrapper.model.app.request.GitHubLinkRequest;
import backend.academy.scrapper.model.db.link.impl.GitHubLink;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class GitHubLinkRequestConverter implements Converter<GitHubLink, GitHubLinkRequest> {
    @Override
    public GitHubLinkRequest convert(GitHubLink source) {
        return new GitHubLinkRequest(source.owner(), source.repo(), source.itemNumber(), source.eventType());
    }
}
