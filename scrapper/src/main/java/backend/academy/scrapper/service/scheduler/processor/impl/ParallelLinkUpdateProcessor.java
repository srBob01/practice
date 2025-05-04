package backend.academy.scrapper.service.scheduler.processor.impl;

import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.service.scheduler.dispatch.LinkDispatchHandler;
import backend.academy.scrapper.service.scheduler.processor.LinkUpdateProcessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.update.processor-type", havingValue = "PARALLEL")
@RequiredArgsConstructor
public class ParallelLinkUpdateProcessor implements LinkUpdateProcessor {
    private final LinkDispatchHandler linkDispatchHandler;
    private final ExecutorService executorService;
    private final int threadCount;

    @Override
    public void process(List<Link> links) {
        List<List<Link>> parts = partition(links, threadCount);
        var futures = new ArrayList<CompletableFuture<Void>>(parts.size());
        for (List<Link> part : parts) {
            futures.add(CompletableFuture.runAsync(
                    () -> {
                        for (Link link : part) {
                            try {
                                linkDispatchHandler.handleOne(link);
                            } catch (Exception e) {
                                log.error("Error processing link {} in parallel", link.id(), e);
                            }
                        }
                    },
                    executorService));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.info("Parallel batch processing complete");
    }

    private <T> List<List<T>> partition(List<T> list, int parts) {
        if (list.isEmpty() || parts <= 0) {
            return new ArrayList<>(Collections.singletonList(list));
        }
        int size = list.size();
        int chunk = (int) Math.ceil((double) size / parts);
        List<List<T>> result = new ArrayList<>();
        for (int i = 0; i < size; i += chunk) {
            result.add(list.subList(i, Math.min(i + chunk, size)));
        }
        return result;
    }
}
