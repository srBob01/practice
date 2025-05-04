package backend.academy.scrapper.model.db.link.impl;

import backend.academy.scrapper.model.db.link.Link;
import backend.academy.scrapper.model.db.link.LinkType;
import backend.academy.scrapper.model.db.link.impl.type.GitHubEventType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "github_link")
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue("GITHUB")
@NoArgsConstructor
@Getter
@Setter
public class GitHubLink extends Link {
    private String owner;
    private String repo;
    private String itemNumber; // Номер Issue или PR

    @Enumerated(EnumType.STRING)
    private GitHubEventType eventType;

    public GitHubLink(String originalUrl, String owner, String repo, String itemNumber, GitHubEventType eventType) {
        super(originalUrl);
        this.owner = owner;
        this.repo = repo;
        this.itemNumber = itemNumber;
        this.eventType = eventType;
    }

    @Override
    public LinkType getType() {
        return LinkType.GITHUB;
    }
}
