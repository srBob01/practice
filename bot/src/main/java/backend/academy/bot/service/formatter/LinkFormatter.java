package backend.academy.bot.service.formatter;

import backend.academy.dto.LinkResponse;
import backend.academy.dto.LinkUpdate;
import backend.academy.dto.ListLinksResponse;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class LinkFormatter {

    public String formatLink(LinkResponse linkResponse) {
        StringBuilder sb = new StringBuilder("🔗 ").append(linkResponse.url());
        if (linkResponse.tags().isEmpty()) {
            sb.append(" (there are no tags)");
        } else {
            sb.append(" (tags:").append(String.join(", ", linkResponse.tags())).append(")");
        }
        return sb.toString();
    }

    public String formatList(ListLinksResponse response) {
        StringBuilder sb = new StringBuilder("Your tracked links:\n");
        for (LinkResponse link : response.links()) {
            sb.append("- ").append(formatLink(link)).append("\n");
        }
        sb.append("\nTotal: ").append(response.size());
        return sb.toString();
    }

    public String formatListForTag(String tag, List<LinkResponse> links) {
        StringBuilder sb = new StringBuilder("Links with the tag '").append(tag).append("':\n");
        for (LinkResponse link : links) {
            sb.append("- ").append(link.url()).append("\n");
        }
        sb.append("\nTotal: ").append(links.size());
        return sb.toString();
    }

    public String formatUpdate(LinkUpdate update) {
        return "🔗 " + update.url() + "\n" + update.description();
    }
}
