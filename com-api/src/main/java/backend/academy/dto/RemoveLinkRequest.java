package backend.academy.dto;

import jakarta.validation.constraints.NotBlank;

/** DTO для запроса на удаление ссылки. */
public record RemoveLinkRequest(@NotBlank(message = "Link must not be blank") String link) {}
