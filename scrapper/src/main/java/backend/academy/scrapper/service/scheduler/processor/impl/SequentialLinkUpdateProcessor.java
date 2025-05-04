package backend.academy.scrapper.service.scheduler.processor.impl;

import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.service.scheduler.dispatch.LinkDispatchHandler;
import backend.academy.scrapper.service.scheduler.processor.LinkUpdateProcessor;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.update.processor-type", havingValue = "SEQUENTIAL", matchIfMissing = true)
public class SequentialLinkUpdateProcessor implements LinkUpdateProcessor {
    private final LinkDispatchHandler linkDispatchHandler;

    @Override
    public void process(List<Link> links) {
        for (Link link : links) {
            linkDispatchHandler.handleOne(link);
        }
    }
}
