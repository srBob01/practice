package backend.academy.scrapper.checkupdate.handler.impl;

import backend.academy.scrapper.checkupdate.handler.LinkUpdateHandler;
import backend.academy.scrapper.checkupdate.worker.so.StackOverflowUpdateService;
import backend.academy.scrapper.converter.base.impl.StackOverflowLinkRequestConverter;
import backend.academy.scrapper.model.app.update.UpdateDetail;
import backend.academy.scrapper.model.db.link.LinkType;
import backend.academy.scrapper.model.db.link.impl.StackOverflowLink;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StackOverflowLinkUpdateHandler implements LinkUpdateHandler<StackOverflowLink> {

    private final StackOverflowUpdateService stackOverflowUpdateService;
    private final StackOverflowLinkRequestConverter requestConverter;

    @Override
    public LinkType getSupportedType() {
        return LinkType.STACKOVERFLOW;
    }

    @Override
    public UpdateDetail fetchUpdateDetail(StackOverflowLink link) {
        var request = requestConverter.convert(link);
        assert request != null;
        return stackOverflowUpdateService.fetchLatestUpdateDetail(request);
    }
}
