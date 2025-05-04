package backend.academy.scrapper.repository.jdbc.mapper.helper.impl;

import backend.academy.scrapper.model.db.link.LinkType;
import backend.academy.scrapper.model.db.link.impl.StackOverflowLink;
import backend.academy.scrapper.repository.jdbc.mapper.helper.AbstractLinkBaseMapper;
import org.springframework.stereotype.Component;

@Component
public class StackOverflowLinkBaseMapper extends AbstractLinkBaseMapper<StackOverflowLink> {
    @Override
    public LinkType getSupportedType() {
        return LinkType.STACKOVERFLOW;
    }

    @Override
    protected StackOverflowLink createEmptyInstance() {
        return new StackOverflowLink();
    }
}
