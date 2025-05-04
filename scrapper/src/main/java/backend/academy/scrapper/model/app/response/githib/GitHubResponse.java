package backend.academy.scrapper.model.app.response.githib;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GitHubResponse {
    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    private String title;
    private String body;
    private GitHubUser user;

    // Дополнительные поля для репозитория
    private String name;
    private String description;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GitHubUser {
        private String login;
    }
}
