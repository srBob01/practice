package backend.academy.scrapper.converter.base.impl;

import backend.academy.scrapper.converter.base.StackOverflowResponseConverter;
import backend.academy.scrapper.model.app.response.so.StackOverflowAnswerDetail;
import backend.academy.scrapper.model.app.response.so.StackOverflowQuestionDetail;
import backend.academy.scrapper.model.app.update.impl.StackOverflowUpdateDetail;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.stereotype.Component;

@Component
public class DefaultStackOverflowResponseConverter implements StackOverflowResponseConverter {
    @Override
    public StackOverflowUpdateDetail convert(
            StackOverflowQuestionDetail questionDetail, StackOverflowAnswerDetail answerDetail) {
        LocalDateTime creationTime =
                LocalDateTime.ofInstant(Instant.ofEpochSecond(answerDetail.creationDate()), ZoneOffset.UTC);
        String preview =
                answerDetail.body().length() > 200 ? answerDetail.body().substring(0, 200) : answerDetail.body();
        return new StackOverflowUpdateDetail(
                questionDetail.title(), answerDetail.owner().displayName(), creationTime, preview);
    }
}
