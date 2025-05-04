package backend.academy.bot.controller;

import backend.academy.bot.service.notification.NotificationService;
import backend.academy.dto.LinkUpdate;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/updates")
public class BotController {

    /** Сервис, отвечающий за логику отправки уведомлений. */
    private final NotificationService notificationService;

    /**
     * Принимает объект {@link LinkUpdate}, проверяет его валидность и передаёт в
     * {@link NotificationService#handleUpdate(LinkUpdate)} для дальнейшей обработки.
     *
     * @param linkUpdate DTO с информацией об обновлении ссылки; не может быть null и должен соответствовать всем
     *     ограничениям валидации.
     * @return строковое представление уникального идентификатора обработанного обновления.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public String sendUpdate(@Valid @RequestBody LinkUpdate linkUpdate) {
        notificationService.handleUpdate(linkUpdate);
        return linkUpdate.id().toString();
    }
}
