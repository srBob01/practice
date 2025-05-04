package backend.academy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/** DTO для запроса на добавление ссылки. */
public record AddLinkRequest(
        @NotBlank(message = "Link must not be blank") String link,
        @NotNull(message = "Tags list must not be null") @Size(min = 0, message = "Tags list must be provided")
                List<@NotBlank(message = "Tag must not be blank") String> tags,
        @NotNull(message = "Filters list must not be null") @Size(min = 0, message = "Filters list must be provided")
                List<@NotBlank(message = "Filter must not be blank") String> filters) {}
