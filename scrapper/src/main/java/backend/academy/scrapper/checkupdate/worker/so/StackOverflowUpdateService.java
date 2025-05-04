package backend.academy.scrapper.checkupdate.worker.so;

import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.converter.base.StackOverflowResponseConverter;
import backend.academy.scrapper.model.app.request.StackOverflowLinkRequest;
import backend.academy.scrapper.model.app.response.so.StackOverflowAnswerDetail;
import backend.academy.scrapper.model.app.response.so.StackOverflowAnswerDetailWrapper;
import backend.academy.scrapper.model.app.response.so.StackOverflowQuestionDetail;
import backend.academy.scrapper.model.app.update.impl.StackOverflowUpdateDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class StackOverflowUpdateService {

    private final RestClient restClient;
    private final StackOverflowResponseConverter stackOverflowResponseConverter;
    private final String stackOverflowKey;
    private final String stackOverflowAccessToken;

    public StackOverflowUpdateService(
            RestClient restClient,
            ScrapperConfig scrapperConfig,
            StackOverflowResponseConverter stackOverflowResponseConverter) {
        this.restClient = restClient;
        this.stackOverflowResponseConverter = stackOverflowResponseConverter;
        this.stackOverflowKey = scrapperConfig.stackOverflow().key();
        this.stackOverflowAccessToken = scrapperConfig.stackOverflow().accessToken();
    }

    /**
     * Получает детальную информацию по обновлению (последний ответ) для вопроса StackOverflow, используя
     * StackOverflowLinkRequest.
     */
    public StackOverflowUpdateDetail fetchLatestUpdateDetail(StackOverflowLinkRequest request) {
        String questionId = request.questionId();

        // Формируем URL для получения деталей вопроса
        String questionUrl = String.format(
                "/2.3/questions/%s?site=stackoverflow&filter=!)Q2B_A7tT0)5rwkNz6Wv&key=%s&access_token=%s",
                questionId, stackOverflowKey, stackOverflowAccessToken);

        // Синхронный запрос для получения информации о вопросе
        StackOverflowQuestionDetail questionDetail =
                restClient.get().uri(questionUrl).retrieve().body(StackOverflowQuestionDetail.class);

        // Формируем URL для получения последнего ответа
        String answerUrl = String.format(
                "/2.3/questions/%s/answers?order=desc&sort=creation&site=stackoverflow&filter=withbody", questionId);

        // Синхронный запрос для получения обёртки с ответами
        StackOverflowAnswerDetailWrapper answerWrapper =
                restClient.get().uri(answerUrl).retrieve().body(StackOverflowAnswerDetailWrapper.class);

        // Извлекаем первый (последний по времени) ответ, если он присутствует
        StackOverflowAnswerDetail answerDetail = null;
        if (answerWrapper != null
                && answerWrapper.items() != null
                && !answerWrapper.items().isEmpty()) {
            answerDetail = answerWrapper.items().getFirst();
        }

        // Конвертируем полученные данные в StackOverflowUpdateDetail и возвращаем
        return stackOverflowResponseConverter.convert(questionDetail, answerDetail);
    }
}
