package backend.academy.scrapper.converter.base.impl;

import backend.academy.scrapper.model.app.request.StackOverflowLinkRequest;
import backend.academy.scrapper.model.db.link.impl.StackOverflowLink;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StackOverflowLinkRequestConverter implements Converter<StackOverflowLink, StackOverflowLinkRequest> {
    @Override
    public StackOverflowLinkRequest convert(StackOverflowLink source) {
        return new StackOverflowLinkRequest(source.questionId());
    }
}
