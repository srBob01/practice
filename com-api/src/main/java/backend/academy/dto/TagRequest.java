package backend.academy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TagRequest(
        @NotNull(message = "Chat ID must not be null") Long chatId,
        @NotBlank(message = "Tag must not be blank") String tag) {}
