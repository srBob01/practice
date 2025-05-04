package backend.academy.scrapper.model.db.link;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "link")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
public abstract class Link {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime lastModified;

    private LocalDateTime lastChecked;

    @Column(name = "original_url", nullable = false)
    private String originalUrl;

    @Version
    private Long version;

    protected Link(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public abstract LinkType getType();

    @SuppressWarnings("unused")
    @PrePersist
    protected void onCreate() {
        this.lastChecked = LocalDateTime.now(ZoneId.systemDefault());
    }
}
