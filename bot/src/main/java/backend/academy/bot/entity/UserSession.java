package backend.academy.bot.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class UserSession {
    private State state;
    private String link;
    private List<String> tags;
    private List<String> filters;
    private NotificationMode mode;
    private int digestHour;

    public UserSession(State state) {
        this.state = state;
        this.mode = NotificationMode.IMMEDIATE;
        this.digestHour = 10;
        tags = new ArrayList<>();
        filters = new ArrayList<>();
    }

    @Override
    public String toString() {
        String tagsInfo = (tags == null
                        || tags.isEmpty()
                        || (tags.size() == 1 && tags.getFirst().trim().isEmpty()))
                ? "No tags provided"
                : String.join(", ", tags);
        String filtersInfo = (filters == null
                        || filters.isEmpty()
                        || (filters.size() == 1 && filters.getFirst().trim().isEmpty()))
                ? "No filters provided"
                : String.join(", ", filters);

        return String.format(
                "Mode: %s%nDigest hour: %d%nLink: %s%nTags: %s%nFilters: %s",
                mode, digestHour, link, tagsInfo, filtersInfo);
    }
}
