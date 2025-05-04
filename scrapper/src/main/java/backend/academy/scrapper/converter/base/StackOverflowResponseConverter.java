package backend.academy.scrapper.converter.base;

import backend.academy.scrapper.model.app.response.so.StackOverflowAnswerDetail;
import backend.academy.scrapper.model.app.response.so.StackOverflowQuestionDetail;
import backend.academy.scrapper.model.app.update.impl.StackOverflowUpdateDetail;

public interface StackOverflowResponseConverter {
    StackOverflowUpdateDetail convert(
            StackOverflowQuestionDetail questionDetail, StackOverflowAnswerDetail answerDetail);
}
