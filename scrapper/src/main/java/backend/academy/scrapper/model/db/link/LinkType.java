package backend.academy.scrapper.model.db.link;

import lombok.Getter;

@Getter
public enum LinkType {
    GITHUB("https://github.com/"),
    STACKOVERFLOW("https://stackoverflow.com/");

    private final String prefix;

    LinkType(String prefix) {
        this.prefix = prefix;
    }
}
