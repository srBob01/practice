package backend.academy.scrapper.model.db.link.impl;

import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.model.db.link.LinkType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stackoverflow_link")
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue("STACKOVERFLOW")
@NoArgsConstructor
@Getter
@Setter
public class StackOverflowLink extends Link {
    private String questionId;

    public StackOverflowLink(String originalUrl, String questionId) {
        super(originalUrl);
        this.questionId = questionId;
    }

    @Override
    public LinkType getType() {
        return LinkType.STACKOVERFLOW;
    }
}
