package backend.academy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TagLinkRequest(
        @NotNull(message = "Chat ID must not be null") Long chatId,
        @NotBlank(message = "Link must not be blank") String link,
        @NotBlank(message = "Tag must not be blank") String tag) {}
