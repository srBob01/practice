package backend.academy.scrapper.model.app.response.so;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StackOverflowAnswerDetail {
    @JsonProperty("creation_date")
    private long creationDate;

    private String body;
    private Owner owner;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Owner {
        @JsonProperty("display_name")
        private String displayName;
    }
}
