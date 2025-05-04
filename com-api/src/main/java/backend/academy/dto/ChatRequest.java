package backend.academy.dto;

import jakarta.validation.constraints.NotNull;

public record ChatRequest(@NotNull Long id) {}
