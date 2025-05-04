package backend.academy.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ListLinksResponse(
        @NotNull(message = "Links list must not be null") List<@Valid LinkResponse> links, int size) {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Your tracked links:\n");
        for (LinkResponse linkResponse : links()) {
            sb.append("- ").append(linkResponse.url());
            if (linkResponse.tags().isEmpty()) {
                sb.append(" (тегов нет)");
            } else {
                sb.append(" (теги: ")
                        .append(String.join(", ", linkResponse.tags()))
                        .append(")");
            }
            sb.append("\n");
        }
        sb.append("\nTotal: ").append(size());
        return sb.toString();
    }
}
