package backend.academy.scrapper.converter.base.impl;

import backend.academy.dto.LinkResponse;
import backend.academy.scrapper.model.db.link.Link;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class LinkConverter {
    public LinkResponse convert(Link link, List<String> tags) {
        return new LinkResponse(link.id(), link.originalUrl(), tags);
    }

    public LinkResponse convert(Link link) {
        return new LinkResponse(link.id(), link.originalUrl());
    }
}
