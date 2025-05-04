package backend.academy.scrapper.service.base.impl.jdbc.enrich.helper.impl;

import backend.academy.scrapper.model.db.link.LinkType;
import backend.academy.scrapper.model.db.link.impl.StackOverflowLink;
import backend.academy.scrapper.repository.jdbc.link.impl.LinkImplRepository;
import backend.academy.scrapper.service.base.impl.jdbc.enrich.helper.AbstractLinkDetailsFetcher;
import org.springframework.stereotype.Component;

@Component
public class StackOverflowLinkFetcher extends AbstractLinkDetailsFetcher<StackOverflowLink> {
    public StackOverflowLinkFetcher(LinkImplRepository<StackOverflowLink> repo) {
        super(repo);
    }

    @Override
    public LinkType getSupportedType() {
        return LinkType.STACKOVERFLOW;
    }
}
