package backend.academy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record LinkResponse(
        @NotNull(message = "Id must not be null") Long id,
        @NotBlank(message = "Url must not be blank") String url,
        @NotNull(message = "Tags must not be null") List<@NotBlank(message = "Tag must not be blank") String> tags,
        @NotNull(message = "Filters must not be null")
                List<@NotBlank(message = "Filter must not be blank") String> filters) {

    public LinkResponse(Long id, String url) {
        this(id, url, List.of(), List.of());
    }

    public LinkResponse(Long id, String url, List<String> tags) {
        this(id, url, tags, List.of());
    }

    public LinkResponse(Long id, String url, List<String> tags, List<String> filters) {
        this.id = id;
        this.url = url;
        this.tags = tags != null ? tags : List.of();
        this.filters = filters != null ? filters : List.of();
    }
}
