package backend.academy.bot.entity;

import lombok.Getter;

@Getter
public enum NotificationMode {
    IMMEDIATE("Send a notification as soon as it is detected"),
    DAILY_DIGEST("Send the digest once a day");

    private final String description;

    NotificationMode(String description) {
        this.description = description;
    }
}
