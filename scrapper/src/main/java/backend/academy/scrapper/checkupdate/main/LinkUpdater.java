package backend.academy.scrapper.checkupdate.main;

import backend.academy.scrapper.checkupdate.handler.LinkUpdateHandler;
import backend.academy.scrapper.model.app.update.UpdateDetail;
import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.model.db.link.LinkType;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Компонент-делегат для определения и вызова нужного обработчика обновлений ссылок в зависимости от их типа
 * ({@link LinkType}).
 *
 * <p>Получает из {@code linkUpdateHandlerMap} соответствующий {@link LinkUpdateHandler} по типу ссылки и вызывает его
 * метод {@link LinkUpdateHandler#fetchUpdateDetail(Link)}.
 */
@Component
@RequiredArgsConstructor
public class LinkUpdater {

    private final Map<LinkType, LinkUpdateHandler<? extends Link>> linkUpdateHandlerMap;

    /**
     * Выбирает обработчик по типу переданной ссылки и возвращает детали её последнего обновления.
     *
     * @param link сущность {@link Link}, для которой нужно получить детали обновления
     * @return объект {@link UpdateDetail} с информацией о последнем обновлении
     */
    @SuppressWarnings("unchecked")
    public UpdateDetail fetchLastUpdate(Link link) {
        return ((LinkUpdateHandler<Link>) linkUpdateHandlerMap.get(link.getType())).fetchUpdateDetail(link);
    }
}
