package backend.academy.scrapper.converter.base.impl;

import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinksResponse;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ListLinksResponseConverter implements Converter<List<LinkResponse>, ListLinksResponse> {
    @Override
    public ListLinksResponse convert(@NotNull List<LinkResponse> source) {
        return new ListLinksResponse(source, source.size());
    }
}
