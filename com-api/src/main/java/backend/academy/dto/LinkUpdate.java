package backend.academy.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record LinkUpdate(
        @NotNull(message = "id must not be null") Long id,
        @NotEmpty(message = "url must not be empty") String url,
        @NotEmpty(message = "description must not be empty") String description,
        @NotNull(message = "tags must not be null") List<Long> chatIds) {}
