package backend.academy.scrapper.model.app.response.githib;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GitHubCommentResponse {
    @JsonProperty("created_at")
    private String createdAt;

    private String body;
    private GitHubUser user;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GitHubUser {
        private String login;
    }
}
