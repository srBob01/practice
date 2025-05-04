package backend.academy.scrapper.repository.jdbc.mapper.helper.impl;

import backend.academy.scrapper.model.db.link.LinkType;
import backend.academy.scrapper.model.db.link.impl.GitHubLink;
import backend.academy.scrapper.repository.jdbc.mapper.helper.AbstractLinkBaseMapper;
import org.springframework.stereotype.Component;

@Component
public class GitHubLinkBaseMapper extends AbstractLinkBaseMapper<GitHubLink> {
    @Override
    public LinkType getSupportedType() {
        return LinkType.GITHUB;
    }

    @Override
    protected GitHubLink createEmptyInstance() {
        return new GitHubLink();
    }
}
