package backend.academy.scrapper.model.app.response.so;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StackOverflowQuestionDetail {
    @JsonProperty("title")
    private String title;
}
