package backend.academy.scrapper.model.app.response.so;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StackOverflowAnswerDetailWrapper {
    private List<StackOverflowAnswerDetail> items;
}
