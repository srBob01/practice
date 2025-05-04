package backend.academy.scrapper.model.app.response.githib;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GitHubCommitResponse {
    private String sha;
    private Commit commit;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Commit {
        private String message;
        private Author author;

        @Getter
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Author {
            private String name;

            @JsonProperty("date")
            private String date;
        }
    }
}
